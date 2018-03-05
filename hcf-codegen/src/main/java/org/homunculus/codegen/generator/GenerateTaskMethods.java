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
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.SrcFile;
import org.homunculus.codegen.generator.PreprocessDiscoverBeans.DiscoveryKind;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.container.MethodBinding;
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

        for (SrcFile file : project.getDiscoveredKinds().get(DiscoveryKind.SINGLETON)) {
            //pick up the already generated async controller class - care of invocation order
            String ctrName = "Async" + file.getPrimaryClassName();
            JDefinedClass cl = project.getCodeModel()._getClass(file.getPackageName() + "." + ctrName);
            if (cl == null) {
                throw new RuntimeException(ctrName + " not found in codemodel");
            }
            ClassOrInterfaceDeclaration ctrClass = file.getUnit().getClassByName(file.getPrimaryClassName()).get();
            for (MethodDeclaration method : ctrClass.getMethods()) {
                //ignore everything which is neither public/package private nor instance bound
                if (method.isPrivate() || method.isProtected() || method.isStatic()) {
                    continue;
                }

                if ((method.getType() instanceof ClassOrInterfaceType)) {
                    String fqn = file.getFullQualifiedName(((ClassOrInterfaceType) method.getType()).getName().toString());
                    System.out.println(fqn);
                    //TODO this won't detect a class hierarchy
                    String[] disallowedTypes = {MethodBinding.class.getName(), Task.class.getName(), Future.class.getName(), Binding.class.getName()};
                    for (String c : disallowedTypes) {
                        if (fqn.equals(c)) {
                            throw new LintException("It is not allowed, that a HCF controller exports the type " + c, file, method.getRange().get());
                        }
                    }

                    //TODO this won't detect a class hierarchy
                    if (fqn.equals(ObjectBinding.class.getName())) {
                        //this is handled by GenerateObjectBindings
                        continue;
                    }
                }

                //if we passed our checks, it is fine to assume that we want it as an async instance task method
                String returnType = file.getFullQualifiedName(method.getType().asString());
                AbstractJClass actualReturn = project.getCodeModel().ref(returnType);
                JMethod newMeth = cl.method(JMod.PUBLIC, project.getCodeModel().ref(Task.class).narrow(project.getCodeModel().ref(Result.class).narrow(actualReturn)), method.getNameAsString());

                StringBuilder ptmp = new StringBuilder();
                for (Parameter param : method.getParameters()) {
                    newMeth.param(project.getCodeModel().ref(file.getFullQualifiedName(param.getType().asString())), param.getNameAsString());
                    ptmp.append(param.getNameAsString()).append(", ");
                }
                if (ptmp.length() > 0) {
                    ptmp.setLength(ptmp.length() - 2);
                }
                newMeth.javadoc().add("Uses {@link org.homunculusframework.factory.container.Container#NAME_BACKGROUND_HANDLER} from the current scope to execute {@link " + project.getJavadocReference(file, method) + "} asynchronously.");
                newMeth.body()._return(JExpr.direct("async(ctr -> ctr." + method.getNameAsString() + "(" + ptmp + "))"));
            }

        }
    }
}
