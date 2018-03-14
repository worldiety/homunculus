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

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Method;
import org.homunculus.codegen.parse.Parameter;
import org.homunculus.codegen.parse.Resolver;
import org.homunculus.codegen.parse.javaparser.SrcFile;
import org.homunculus.codegen.generator.PreprocessDiscoverBeans.DiscoveryKind;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.container.MethodBinding;
import org.homunculusframework.factory.container.ModelAndView;
import org.homunculusframework.factory.container.ObjectBinding;
import org.homunculusframework.lang.Result;

import java.util.concurrent.Future;

/**
 * @author Torben Schinke
 * @since 1.0
 */

public class GenerateTaskMethods implements Generator {
    @Override
    public void generate(GenProject project) throws Exception {
        Resolver resolver = project.getResolver();
        for (FullQualifiedName bean : project.getDiscoveredKinds().get(DiscoveryKind.SINGLETON)) {
            //pick up the already generated async controller class - care of invocation order
            String ctrName = "Async" + bean.getSimpleName();
            JDefinedClass cl = project.getCodeModel()._getClass(bean.getPackageName() + "." + ctrName);
            if (cl == null) {
                throw new RuntimeException(ctrName + " not found in codemodel");
            }
            for (Method method : resolver.getMethods(bean)) {
                //ignore everything which is neither public/package private nor instance bound
                if (method.isPrivate() || method.isProtected() || method.isStatic()) {
                    continue;
                }

                FullQualifiedName methodType = method.getType().getFullQualifiedName();
                String[] disallowedTypes = {MethodBinding.class.getName(), Task.class.getName(), Future.class.getName()};
                for (String c : disallowedTypes) {
                    if (resolver.isInstanceOf(methodType, new FullQualifiedName(c))) {
                        throw method.newLintException("It is not allowed, that a HCF controller exports the type " + c);
                    }
                }

                if (resolver.isInstanceOf(methodType, new FullQualifiedName(Binding.class))) {
                    //this is handled by GenerateMethodBinding
                    continue;
                }

                if (resolver.isInstanceOf(methodType, new FullQualifiedName(ModelAndView.class))) {
                    //this is handled by GenerateMethodBinding
                    continue;
                }

//                    System.out.println("should be task ->" + method.getType() + " " + method.getType().getClass() + " [" + fqn + "]");


                //if we passed our checks, it is fine to assume that we want it as an async instance task method
                AbstractJClass actualReturn = project.getCodeModel().ref(method.getType().getFullQualifiedName().toString());
                JMethod newMeth = cl.method(JMod.PUBLIC, project.getCodeModel().ref(Task.class).narrow(project.getCodeModel().ref(Result.class).narrow(actualReturn)), method.getName());

                StringBuilder ptmp = new StringBuilder();
                for (Parameter param : method.getParameters()) {
                    newMeth.param(project.getCodeModel().ref(param.getType().toString()), param.getName());
                    ptmp.append(param.getName()).append(", ");
                }
                if (ptmp.length() > 0) {
                    ptmp.setLength(ptmp.length() - 2);
                }
                newMeth.javadoc().add("Uses {@link org.homunculusframework.factory.container.Container#NAME_BACKGROUND_HANDLER} from the current scope to execute {@link " + method.asJavadocAnchor() + "} asynchronously.");
                newMeth.body()._return(JExpr.direct("async(ctr -> ctr." + method.getName() + "(" + ptmp + "))"));
            }

        }
    }
}
