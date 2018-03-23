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

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JLambda;
import com.helger.jcodemodel.JLambdaParam;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Method;
import org.homunculus.codegen.parse.Parameter;
import org.homunculus.codegen.parse.Resolver;
import org.homunculus.codegen.generator.PreprocessDiscoverBeans.DiscoveryKind;
import org.homunculusframework.concurrent.Cancellable;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.concurrent.NotInterruptible;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.container.MethodBinding;
import org.homunculusframework.factory.container.ModelAndView;
import org.homunculusframework.lang.Result;

import java.util.concurrent.Future;

/**
 * @author Torben Schinke
 * @since 1.0
 */

public class GenerateTaskMethods implements Generator {
    @Override
    public void generate(GenProject project) throws Exception {
        JCodeModel code = project.getCodeModel();
        Resolver resolver = project.getResolver();
        for (FullQualifiedName bean : project.getDiscoveredKinds().get(DiscoveryKind.SINGLETON)) {
            //pick up the already generated async controller class - care of invocation order
            String ctrName = "Async" + bean.getSimpleName();
            JDefinedClass cl = project.getCodeModel()._getClass(bean.getPackageName() + "." + ctrName);
            if (cl == null) {
                throw new RuntimeException(ctrName + " not found in codemodel");
            }
            int idCtr = 0;
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


                newMeth.javadoc().add("Uses {@link org.homunculusframework.factory.container.Container#NAME_BACKGROUND_HANDLER} from the current scope to execute {@link " + method.asJavadocAnchor() + "} asynchronously.");
                JInvocation async = JExpr.invoke("async");
                async.arg(idCtr++ + "-" + method.getName());
                boolean notInterruptible = method.getAnnotation(NotInterruptible.class) != null;
                boolean isCancellable = method.getAnnotation(Cancellable.class) != null;
                if (notInterruptible) {
                    async.arg(cl.staticRef("DO_NOT_INTERRUPT"));
                } else {
                    async.arg(cl.staticRef("MAY_INTERRUPT"));
                }

                if (isCancellable) {
                    async.arg(cl.staticRef("CANCEL_PENDING"));
                } else {
                    async.arg(cl.staticRef("DO_NOT_CANCEL_PENDING"));
                }

                JLambda lambda = new JLambda();
                JLambdaParam ctr = lambda.addParam("ctr");
                JInvocation ctrInvokeMethod = ctr.invoke(method.getName());

                for (Parameter param : method.getParameters()) {
                    JVar asyncMethodParam = newMeth.param(project.getCodeModel().ref(param.getType().toString()), param.getName());
                    ctrInvokeMethod.arg(asyncMethodParam);
                }


                lambda.body().lambdaExpr(ctrInvokeMethod);
                async.arg(lambda);
                newMeth.body()._return(async);
            }

        }
    }
}
