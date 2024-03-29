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

import com.github.javaparser.ast.body.MethodDeclaration;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.generator.PreprocessDiscoverBeans.DiscoveryKind;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculusframework.factory.async.AsyncDelegate;
import org.homunculusframework.factory.container.BackgroundHandler;
import org.homunculusframework.factory.scope.Scope;

/**
 * Creates Async* classes for everything which has been identified as a singleton by {@link PreprocessDiscoverBeans}.
 * <p>
 * This just creates empty classes which are used by {@link GenerateMethodBindings} and {@link GenerateTaskMethods}.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class GenerateAsyncControllers implements Generator {


    @Override
    public void generate(GenProject project) throws Exception {
        for (FullQualifiedName bean : project.getDiscoveredKinds().get(DiscoveryKind.SINGLETON)) {
            JDefinedClass jc = project.getCodeModel().
                    _package(bean.getPackageName()).
                    _class("Async" + bean.getSimpleName());
            AbstractJClass delegateType = project.getCodeModel().ref(bean.toString());
            jc._extends(project.getCodeModel().ref(AsyncDelegate.class).narrow(delegateType));
            jc.headerComment().add(project.getDisclaimer(getClass()));
            jc.javadoc().add("This class provides asynchronous calls for all public methods of {@link " + bean.toString() + "}. \nThis should only be used from the UI and not from within other Controllers.\nIt always expects an injected Scope to determine the lifetime of the task.\nIf you need special behavior or other methods, it is fine to extend this class.");

            //e.g. public AsyncDelegate(Scope scope, BackgroundHandler handler, Delegate delegate) {
            JMethod method = jc.constructor(JMod.PUBLIC);
            JVar varScope = method.param(Scope.class, "scope");
            JVar varHandler = method.param(BackgroundHandler.class, "handler");
            JVar varDelegate = method.param(delegateType, "delegate");

            method.body().invokeSuper().arg(varScope).arg(varHandler).arg(varDelegate);
        }
    }

    static class Method {
        final MethodDeclaration declaration;

        public Method(MethodDeclaration declaration) {
            this.declaration = declaration;
        }
    }

}
