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

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.parse.javaparser.SrcFile;
import org.homunculus.codegen.generator.ObjectBindingGenerator.Field;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private Map<DiscoveryKind, Set<SrcFile>> discover(GenProject project) {
        Map<DiscoveryKind, Set<SrcFile>> discoveryKinds = project.getDiscoveredKinds();
        for (DiscoveryKind kind : DiscoveryKind.values()) {
            discoveryKinds.put(kind, new HashSet<>());
        }
        NEXT_SRC:
        for (SrcFile src : project.getSrcFiles()) {
            Optional<ClassOrInterfaceDeclaration> optDec = src.getUnit().getClassByName(src.getPrimaryClassName());
            if (!optDec.isPresent()) {
                LoggerFactory.getLogger(getClass()).warn("ignored file {}", src.getFile());
                continue;
            }
            ClassOrInterfaceDeclaration dec = optDec.get();


            NodeList<AnnotationExpr> annotations = dec.getAnnotations();
            for (DiscoveryKind kind : DiscoveryKind.values()) {
                for (AnnotationExpr annotation : annotations) {
                    String symbol = src.getFullQualifiedName(annotation.getNameAsString());
                    if (kind.match(symbol)) {
                        discoveryKinds.get(kind).add(src);
                        //also always ensure that a singleton is always generated as a bean
                        if (kind == DiscoveryKind.SINGLETON) {
                            discoveryKinds.get(DiscoveryKind.BEAN).add(src);
                        }
                        continue NEXT_SRC;
                    }
                }

                //now check also our fields
                List<Field> fields = new ArrayList<>();
                ObjectBindingGenerator.collectInjectableFields(project, src, dec, fields);

                for (Field field : fields) {
                    NodeList<AnnotationExpr> fAnnotations = field.dec.getAnnotations();
                    for (AnnotationExpr annotation : fAnnotations) {
                        String symbol = src.getFullQualifiedName(annotation.getNameAsString());
                        if (kind.match(symbol)) {
                            discoveryKinds.get(kind).add(src);
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
        BEAN("javax.inject.Inject", "org.springframework.beans.factory.annotation.Autowired");

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
