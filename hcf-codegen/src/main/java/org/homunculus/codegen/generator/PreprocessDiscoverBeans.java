/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.homunculus.codegen.generator;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Field;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Resolver;
import org.homunculusframework.factory.flavor.hcf.Bind;
import org.homunculusframework.factory.flavor.hcf.ViewComponent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Finds and grabs beans which are somewhat special for (static bound) injection
 * or singleton lifetime scope.
 * <p>
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class PreprocessDiscoverBeans implements Generator {
    @Override
    public void generate(GenProject project) throws Exception {
        discover(project);
    }

    private boolean isAcceptableBean(Resolver resolver, FullQualifiedName fqn) {
        return resolver.isTopLevelType(fqn) || resolver.isStatic(fqn) && (!resolver.isAbstract(fqn) && !resolver.isPrivate(fqn));
    }

    private Map<DiscoveryKind, Set<FullQualifiedName>> discover(GenProject project) throws ClassNotFoundException {
        Map<DiscoveryKind, Set<FullQualifiedName>> discoveryKinds = project.getDiscoveredKinds();
        for (DiscoveryKind kind : DiscoveryKind.values()) {
            discoveryKinds.put(kind, new HashSet<>());
        }
        Resolver resolver = project.getResolver();

        NEXT_SRC:
        for (FullQualifiedName bean : resolver.getTypes()) {
            if (isAcceptableBean(resolver, bean)) {
                //check type annotations to distinguish some basic behaviors, especially singleton stuff
                List<Annotation> typeAnnotations = resolver.getAnnotations(bean);

                DiscoveryKind[] annotationKinds = {DiscoveryKind.SINGLETON, DiscoveryKind.BEAN, DiscoveryKind.BIND};
                for (DiscoveryKind kind : annotationKinds) {
                    //check if we have a type annotation to avoid parsing all fields
                    for (Annotation annotation : typeAnnotations) {
                        if (kind.match(annotation.getFullQualifiedName().toString())) {
                            discoveryKinds.get(kind).add(bean);
                            //also always ensure that a singleton is always generated as a bean
                            if (kind == DiscoveryKind.SINGLETON) {
                                discoveryKinds.get(DiscoveryKind.BEAN).add(bean);
                            }
                            continue NEXT_SRC;
                        }
                    }

                    //other beans may have no annotation at all, like UISs -> we can only find those if they have field annotations
                    for (Field field : resolver.getFields(bean)) {
                        for (Annotation annotation : field.getAnnotations())
                            if (kind.match(annotation.getFullQualifiedName().toString())) {
                                discoveryKinds.get(kind).add(bean);
                            }
                    }
                }


                DiscoveryKind[] superTypeKinds = {DiscoveryKind.APPLICATION, DiscoveryKind.ACTIVITY};
                for (DiscoveryKind kind : superTypeKinds) {
                    for (String fqn : kind.fullQualifiedNames) {
                        if (resolver.isInstanceOf(bean, new FullQualifiedName(fqn))) {
                            discoveryKinds.get(kind).add(bean);
                            continue NEXT_SRC;
                        }
                    }

                }


            }
        }


        return discoveryKinds;
    }


    public enum DiscoveryKind {
        /**
         * These class annotations indicate singletons which needs to be created within a single factory
         */
        SINGLETON("org.springframework.stereotype.Controller", "org.springframework.stereotype.Service", "javax.inject.Singleton"),
        /**
         * All classes which have fields annotated with this, must have ObjectBindings. This includes also singletons.
         * Beans without such annotations need no binding, because they have a typesafe constructor or setters. However SINGLETONs are
         * always included
         */
        BEAN("javax.inject.Inject", "org.springframework.beans.factory.annotation.Autowired", ViewComponent.class.getName()),

        /**
         * Classes annotated with @Bind will be used as Binding candidates and get each their scope with the activity scope as parent.
         */
        BIND(Bind.class.getName()),

        /**
         * super classes (not annotations) to be treated as app. They get their scope with singleton constructors
         */
        APPLICATION("org.homunculus.android.component.HomunculusApplication", "android.app.Application"),

        /**
         * Super classes (not annotations) to be treated as activity. They get each their scopes with the application scope as parent.
         */
        ACTIVITY("org.homunculus.android.component.HomunculusActivity", "android.app.Activity");

        private String[] fullQualifiedNames;

        DiscoveryKind(String... args) {
            fullQualifiedNames = args;
        }

        public boolean match(String name) {
            for (String n : fullQualifiedNames) {
                if (n.equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }
}
