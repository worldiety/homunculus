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
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
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
import org.homunculusframework.scope.Scope;

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
                for (String a : allowedReturnValues) {
                    if (method.getType().getFullQualifiedName().toString().equals(a)) {
                        allowed = true;
                        break;
                    }
                }
                if (!allowed) {
                    continue;
                }

                //create the inner binding class
                JDefinedClass binding = cl._class(JMod.STATIC | JMod.PUBLIC, "Bind" + bean.getSimpleName() + Strings.startUpperCase(method.getName()));
                binding.javadoc().add("A decoupled binding to {@link " + method.asJavadocAnchor() + "} which is serializable.");

                binding._extends(project.getCodeModel().ref(MethodBinding.class).narrow(Object.class));

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

                //override onBind for the scope
                JMethod onBind = binding.method(JMod.PROTECTED, void.class, "onBind");
                JVar dstVar = onBind.param(Scope.class, "dst");
                onBind.annotate(project.getCodeModel().ref(Override.class));
                for (Parameter p : method.getParameters()) {
                    onBind.body().add(dstVar.invoke("put").arg(p.getName()).arg(binding.fields().get(p.getName())));
                }

                //override onExecute which performs the actual calling
                JMethod onExecute = binding.method(JMod.PROTECTED, project.getCodeModel().ref(ObjectBinding.class).narrow(Object.class), "onExecute")._throws(Exception.class);
                onExecute.annotate(project.getCodeModel().ref(Override.class));
                JVar ctr = onExecute.body().decl(project.getCodeModel().ref(bean.toString()), "_ctr");
                onExecute.body().directStatement(ctr.name() + " = get(" + bean.toString() + ".class);");
                onExecute.body().invoke("assertNotNull").arg(project.getCodeModel().ref(bean.toString()).dotclass()).arg(ctr);
                StringBuilder tmp = new StringBuilder();
                tmp.append("(ObjectBinding<Object>) (ObjectBinding<?>)");
                tmp.append(ctr.name()).append(".").append(method.getName()).append("(");
                for (Parameter p : method.getParameters()) {
                    tmp.append(p.getName()).append(", ");
                }
                if (tmp.charAt(tmp.length() - 2) == ',') {
                    tmp.setLength(tmp.length() - 2);
                }
                tmp.append(")");
                onExecute.body()._return(JExpr.direct(tmp.toString()));
            }

        }
    }
}
