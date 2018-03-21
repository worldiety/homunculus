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
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.generator.PreprocessDiscoverBeans.DiscoveryKind;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Method;
import org.homunculus.codegen.parse.Parameter;
import org.homunculus.codegen.parse.Strings;
import org.homunculusframework.factory.container.MethodBinding;
import org.homunculusframework.factory.container.ModelAndView;
import org.homunculusframework.factory.container.ObjectBinding;

/**
 * Generates inner classes in Async* controllers which are created by {@link GenerateAsyncControllers}.
 * The output looks like the following excerpt:
 * <p>
 * <pre>
 * public static class BindCartControllerGetCart2 extends MethodBinding< Object> {
 *
 *  private int cartId;
 *
 *  public BindCartControllerGetCart2(int cartId) {
 *    this.cartId = cartId;
 *  }
 *
 *  protected void onBind(Scope dst) {
 *    dst.put("cartId", cartId);
 *  }
 *
 *  protected ObjectBinding< Object> onExecute() throws Exception {
 *    CartController ctr = get(CartController.class);
 *    assertNotNull(CartController.class, ctr);
 *    return (ObjectBinding< Object>) (ObjectBinding<?>) ctr.getCart3(cartId);
 *  }
 *
 * }
 * </pre>
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class GenerateMethodBindings implements Generator {
    @Override
    public void generate(GenProject project) throws Exception {
        JCodeModel code = project.getCodeModel();
        FullQualifiedName application = project.getDiscoveredKinds().get(DiscoveryKind.APPLICATION).iterator().next();
        FullQualifiedName commonActivityContract = new FullQualifiedName(application.getPackageName() + ".ActivityScope");
        for (FullQualifiedName bean : project.getDiscoveredKinds().get(DiscoveryKind.SINGLETON)) {
            //pick up the already generated async controller class - care of invocation order
            String ctrName = "Async" + bean.getSimpleName();
            JDefinedClass cl = project.getCodeModel()._getClass(bean.getPackageName() + "." + ctrName);
            if (cl == null) {
                throw new RuntimeException(ctrName + " not found in codemodel");
            }

            for (Method method : project.getResolver().getMethods(bean)) {
                //ignore everything which is neither public/package private nor instance bound
                if (method.isPrivate() || method.isProtected() || method.isStatic()) {
                    continue;
                }


                String[] allowedReturnValues = {ObjectBinding.class.getName(), ModelAndView.class.getName()};
                boolean allowed = false;
                for (String c : allowedReturnValues) {
                    if (project.getResolver().isInstanceOf(method.getType().getFullQualifiedName(), new FullQualifiedName(c))) {
                        allowed = true;

                        break;
                    }
                }

                if (!allowed) {
                    continue;
                }

                //create the inner binding class
                JDefinedClass binding = cl._class(JMod.STATIC | JMod.PUBLIC, "Invoke" + bean.getSimpleName() + Strings.startUpperCase(method.getName()));
                binding.javadoc().add("A decoupled binding to {@link " + method.asJavadocAnchor() + "} which is serializable.");


                AbstractJClass activityScope = code.ref(commonActivityContract.toString()).narrowAny();
                binding._extends(project.getCodeModel().ref(MethodBinding.class).narrow(activityScope));

                //add the constructor for this binding
                JMethod con = binding.constructor(JMod.PUBLIC);
                for (Parameter p : method.getParameters()) {
                    AbstractJClass pType = project.getCodeModel().ref(p.getType().toString());

                    //the field
                    JFieldVar thisVar = binding.field(JMod.PRIVATE, pType, p.getName());

                    //the constructor param
                    JVar pVar = con.param(pType, p.getName());

                    //the setting
                    con.body().assign(JExpr._this().ref(thisVar), pVar);
                }


//                public static class InvokeControllerBQueryWithBindingDelegate
//                        extends MethodBinding<ActivityScope<?>>
//                {
//                    private String param;
//
//                    public InvokeControllerBQueryWithBindingDelegate(String param) {
//                        this.param = param;
//                    }
//
//
//                    @Override
//                    public ObjectBinding<?, ?> create(ActivityScope<?> scope) throws Exception {
//                        return scope.getParent().getControllerB().queryWithBindingDelegate(param);
//                    }
//                }
                //override onExecute which performs the actual calling
                JMethod onExecute = binding.method(JMod.PUBLIC, project.getCodeModel().ref(ObjectBinding.class).narrowAny().narrowAny(), "create")._throws(Exception.class);
                onExecute.annotate(project.getCodeModel().ref(Override.class));
                JVar scopeVar = onExecute.param(activityScope, "scope");

                JInvocation getCtr = scopeVar.invoke("getParent").invoke("get" + bean.getSimpleName()).invoke(method.getName());
                for (JFieldVar field : binding.fields().values()) {
                    getCtr.arg(field);
                }
                onExecute.body()._return(getCtr);


                binding.method(JMod.PUBLIC, String.class, "toString").body()._return(JExpr.lit(bean.getSimpleName() + "." + method.getName()));
            }

        }
    }
}
