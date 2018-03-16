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
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCatchBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JLambda;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JTryBlock;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Constructor;
import org.homunculus.codegen.parse.Field;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Method;
import org.homunculus.codegen.parse.Parameter;
import org.homunculus.codegen.parse.Resolver;
import org.homunculus.codegen.parse.Strings;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.ModelAndView;
import org.homunculusframework.factory.container.ObjectBinding;
import org.homunculusframework.factory.flavor.hcf.Execute;
import org.homunculusframework.factory.flavor.hcf.FactoryParam;
import org.homunculusframework.factory.flavor.hcf.Priority;
import org.homunculusframework.factory.flavor.hcf.ViewComponent;
import org.homunculusframework.lang.Reflection;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * This guy creates a type safe class to create instances of a Bean or Pojo. You can mix constructor and Field injection.
 * Typesafe fields must be annotated with {@link FactoryParam}, other injects are resolved at runtime.
 * <p>
 * Creates Factories for Beans which extend from {@link ObjectBinding}
 * and are named Bind*.
 * <p>
 * The output looks like this:
 * <pre>
 * public static class BindCartUIS extends ObjectBinding<CartUIS> {
 *
 * private static Field fieldSomeString;
 * private static Field fieldSomeOptionalString;
 * private static Field fieldCartModel;
 *
 * private String someString;
 * private String someOptionalString;
 * private CartModel cartModel;
 *
 *  public BindCartUIS(String someString, @Nullable String someOptionalString, CartModel cartModel) {
 *   this.someString = someString;
 *   this.someOptionalString = someOptionalString;
 *   this.cartModel = cartModel;
 *  }
 *
 *  protected void initStatic() {
 *   Map<String, Field> fields = Reflection.getFieldsMap(CartUIS.class);
 *   fieldSomeString = fields.get("someString");
 *   fieldSomeOptionalString = fields.get("someOptionalString");
 *   fieldCartModel = fields.get("cartModel");
 *  }
 *
 *  protected void onBind(Scope dst) {
 *   dst.put("someString", someString);
 *   dst.put("someOptionalString", someOptionalString);
 *   dst.put("cartModel", cartModel);
 *  }
 *
 *  protected CartUIS onExecute() throws Exception {
 *   CartUIS obj = new CartUIS(get(Context.class));
 *   fieldSomeString.set(obj, get("someString", String.class));
 *   fieldSomeOptionalString.set(obj, get("someString", String.class));
 *   fieldCartModel.set(obj, get("cartModel", CartModel.class));
 *   return obj;
 *  }
 * }
 * </pre>
 *
 * @author Torben Schinke
 * @since 1.0
 */
class ObjectBindingGenerator {
    void create(GenProject project, FullQualifiedName bean) throws Exception {
        Resolver resolver = project.getResolver();
        //pick up the already generated async controller class - care of invocation order
        String ctrName = "Bind" + bean.getSimpleName();
        JDefinedClass binding = project.getCodeModel()._class(bean.getPackageName() + "." + ctrName);

        binding.javadoc().add("A decoupled binding to {@link " + bean + "} which uses a {@link " + Scope.class.getName() + "} and type safe parameters to create an instance.");
        Class what = resolver.getAnnotation(bean, ViewComponent.class) != null ? ModelAndView.class : ObjectBinding.class;
        AbstractJClass delegate = project.getCodeModel().ref(bean.toString());
        binding._extends(project.getCodeModel().ref(what).narrow(delegate));

        ObjectBindingModel objectBindingModel = new ObjectBindingModel();
        objectBindingModel.bean = bean;

        //pick all available fields, including super classes
        List<Field> fields = resolver.getFields(bean);
        HashMap<String, Field> lintFieldNames = new HashMap<>();
        for (Field field : fields) {
            if (lintFieldNames.containsKey(field.getName())) {
                Field other = lintFieldNames.get(field.getName());
                LintException e = field.newLintException("field name '" + field.getName() + "' is ambiguous across inheritance");
                e.addSuppressed(other.newLintException("other field is"));
                throw e;
            }
        }

        //pick normal injection and constructor fields
        for (Field f : fields) {
            if (f.getAnnotation(FactoryParam.class) != null) {
                objectBindingModel.constructorFields.add(f);
                continue;
            }
            if (f.getAnnotation(Inject.class) != null || f.getAnnotation(Autowired.class) != null) {
                objectBindingModel.fields.add(f);
            }
        }

        //pick the shortest constructor
        for (Constructor c : resolver.getConstructors(bean)) {
            if (objectBindingModel.constructor == null || objectBindingModel.constructor.getParameters().size() > c.getParameters().size()) {
                objectBindingModel.constructor = c;
            }
        }

        //create stub for the onExecute method
        OnExecuteMethod onExecuteMethod = createOnExecuteMethod(project.getCodeModel(), binding, bean);

        //the constructor is build from the original constructor and special annotated fields
        createTypeSafeBindingConstructor(project.getCodeModel(), onExecuteMethod, binding, objectBindingModel);

        //insert other injections, pure reflection nothing special
        createReflectionInjectSetters(project.getCodeModel(), binding, objectBindingModel, onExecuteMethod);


        onExecuteMethod.onExecute.body()._return(onExecuteMethod.varBean);


        //the postConstructs
        List<Method> methods = project.getResolver().getMethods(bean);
        for (Method m : methods) {
            Annotation postConstruct = m.getAnnotation(new FullQualifiedName(PostConstruct.class));
            if (postConstruct != null) {
                assertLifecycleMethod(m);
                objectBindingModel.postConstruct.add(m);
            }
        }
        sortMethodCalls(objectBindingModel.postConstruct);
        generatePostExecute(delegate, objectBindingModel.postConstruct, project.getCodeModel(), binding);

        //the preDestroys
        for (Method m : methods) {
            Annotation preDestroy = m.getAnnotation(new FullQualifiedName(PreDestroy.class));
            if (preDestroy != null) {
                assertLifecycleMethod(m);
                objectBindingModel.preDestroy.add(m);
            }
        }
        sortMethodCalls(objectBindingModel.preDestroy);
        generatePreDestroy(delegate, objectBindingModel.preDestroy, project.getCodeModel(), binding);


    }


    private OnExecuteMethod createOnExecuteMethod(JCodeModel code, JDefinedClass binding, FullQualifiedName bean) {
        OnExecuteMethod exec = new OnExecuteMethod();
        //        override onExecute which performs the actual calling
        exec.onExecute = binding.method(JMod.PROTECTED, code.ref(bean.toString()), "onExecute")._throws(Exception.class);
        exec.onExecute.annotate(code.ref(Override.class));
        exec.varBean = exec.onExecute.body().decl(code.ref(bean.toString()), "_bean");
        exec.newBeanStatement = JExpr._new(code.ref(bean.toString()));
        exec.onExecute.body().assign(exec.varBean, exec.newBeanStatement);
        return exec;
    }


    private void createTypeSafeBindingConstructor(JCodeModel code, OnExecuteMethod onExecuteMethod, JDefinedClass binding, ObjectBindingModel model) {
        JMethod c = binding.constructor(JMod.PUBLIC);
        c.javadoc().add("creates a decoupled binding to {@link " + model.bean + "}");
        if (model.constructor != null) {
            for (Parameter p : model.constructor.getParameters()) {
                if (p.getAnnotation(FactoryParam.class) == null) {
                    //a "stateless" parameter, which is required to be resolved at runtime

                    //add the actual param as a dynamic type
                    onExecuteMethod.newBeanStatement.arg(JExpr.invoke("get").arg(code.ref(p.getType().toString()).dotclass()));
                } else {
                    //a stateful parameter, treated as a field
                    AbstractJClass cl = code.ref(p.getType().toString());
                    JVar paramVar = c.param(cl, p.getName());
                    JFieldVar fieldVar = binding.field(JMod.PRIVATE, cl, p.getName());
                    fieldVar.javadoc().add("considered as stateful, introduced by {@link " + model.constructor.asJavadocAnchor() + "}");
                    c.body().assign(JExpr.refthis(fieldVar), paramVar);

                    //add the actual param also to the constructor
                    onExecuteMethod.newBeanStatement.arg(paramVar);
                }

            }
        }

        for (Field field : model.constructorFields) {
            AbstractJClass cl = code.ref(field.getType().getFullQualifiedName().toString());
            JVar paramVar = c.param(cl, field.getName());
            JFieldVar fieldVar = binding.field(JMod.PRIVATE, cl, field.getName());
            fieldVar.javadoc().add("conceptually stateful, declared at {@link " + field.asJavadocAnchor() + "}");
            c.body().assign(JExpr.refthis(fieldVar), paramVar);
            boolean samePackage = field.getDeclaringType().getPackageName().equals(model.bean.getPackageName());
            if ((field.isDefault() && samePackage) || field.isPublic()) {
                //cool, we have direct field access to avoid any reflection
                onExecuteMethod.onExecute.body().add(JExpr.assign(onExecuteMethod.varBean.ref(fieldVar.name()), fieldVar));
            } else {
                //oh no, we have to add reflection for our stateful fields
                JMethod setter = insertReflectiveFieldSetter(code, binding, onExecuteMethod, field);
                onExecuteMethod.onExecute.body().invoke(setter).arg(onExecuteMethod.varBean).arg(fieldVar);
            }
        }


    }

    private void createReflectionInjectSetters(JCodeModel code, JDefinedClass binding, ObjectBindingModel model, OnExecuteMethod onExecuteMethod) {
        for (Field field : model.fields) {
            JMethod setter = insertReflectiveFieldSetter(code, binding, onExecuteMethod, field);
            AbstractJClass type = code.ref(field.getType().getFullQualifiedName().toString());
            Annotation named = field.getAnnotation(Named.class);
            if (named != null) {
                String beanName = named.getString("");
                onExecuteMethod.onExecute.body().invoke(setter).arg(onExecuteMethod.varBean).arg(JExpr.invoke("get").arg(beanName).arg(type.dotclass()));
            } else {
                JBlock exec = onExecuteMethod.onExecute.body();
                JInvocation getFromScope = JExpr.invoke("get").arg(type.dotclass());
                JVar tmpVar = exec.decl(type, "_" + field.getName());
                exec.assign(tmpVar, getFromScope);
                exec._if(tmpVar.eqNull())._then().block().assign(tmpVar, JExpr._new(type));
                exec.invoke(setter).arg(onExecuteMethod.varBean).arg(tmpVar);
            }

        }
    }

    private JMethod insertReflectiveFieldSetter(JCodeModel code, JDefinedClass binding, OnExecuteMethod onExecuteMethod, Field field) {
        JMethod setter = binding.method(JMod.PRIVATE, void.class, "set" + Strings.startUpperCase(field.getName()))._throws(IllegalAccessException.class);
        setter.javadoc().add("See {@link " + field.asJavadocAnchor() + "}");
        JVar bean = setter.param(code.ref(field.getDeclaringType().toString()), "_bean");
        JVar value = setter.param(code.ref(field.getType().getFullQualifiedName().toString()), field.getName());

        JFieldVar refField = binding.field(JMod.PRIVATE | JMod.STATIC, java.lang.reflect.Field.class, "field" + Strings.startUpperCase(field.getName()));
        refField.javadoc().add("conceptually stateless injection, declared at {@link " + field.asJavadocAnchor() + "}");

        AbstractJClass fieldType = code.ref(field.getDeclaringType().toString());
        JBlock initBlock = setter.body()._if(refField.eqNull())._then().synchronizedBlock(JExpr._this()).body()._if(refField.eqNull())._then();
        initBlock.assign(refField, code.ref(Reflection.class).staticInvoke("getField").arg(fieldType.dotclass()).arg(field.getName()));
        initBlock.add(refField.invoke("setAccessible").arg(true));
        setter.body().add(refField.invoke("set").arg(bean).arg(value));

        return setter;
    }

    private void assertLifecycleMethod(Method m) throws LintException {
        if (m.isStatic()) {
            throw m.newLintException("Method must not be static");
        }

        if (m.isAbstract()) {
            throw m.newLintException("Method may not be abstract");
        }

        if (m.getParameters().size() > 0) {
            throw m.newLintException("Method is not allowed to have parameters");
        }
    }

    /**
     * Example:
     * <pre>
     * protected void onPostExecute(SettableTask<Result<CartUIS>> task, @Nullable CartUIS cartUIS, @Nullable Throwable t){
     *   post("hallo", () -> {
     *    delegateBlub0(cartUIS);
     *    post("main", () -> {
     *     delegateBlub1(cartUIS);
     *     onPostExecuteDone();
     *    });
     *   });
     * }
     * </pre>
     *
     * @param methods
     * @param code
     * @param binding
     */
    private void generatePostExecute(AbstractJClass param, List<Method> methods, JCodeModel code, JDefinedClass binding) {
        JMethod override = binding.method(JMod.PROTECTED, void.class, "onPostExecute");
        JVar task = override.param(code.ref(SettableTask.class).narrow(code.ref(Result.class).narrow(param)), "task");
        JVar res = override.param(param, "res");
        JVar exception = override.param(code.ref(Throwable.class), "throwable");
        override.annotate(Override.class);

        JBlock block = override.body();
        JBlock errExit = block._if(exception.neNull())._then();
        errExit.invoke(task, "set").arg(code.ref(Result.class).staticInvoke("create").arg(res).invoke("setThrowable").arg(exception));
        errExit._return();

        if (methods.isEmpty()) {
            block.add(JExpr._super().invoke("onPostExecute").arg(task).arg(res).arg(exception));
            return;
        }

        int ctr = 0;
        for (Method m : methods) {
            JLambda lambda = new JLambda();
            String name = generateMethodCall(param, m, code, binding);
            JTryBlock tryBlock = lambda.body()._try();
            tryBlock.body().add(JExpr.invoke(name).arg(res));
            JCatchBlock catchBlock = tryBlock._catch(code.ref(Throwable.class));
            JVar x = catchBlock.param("e" + ctr);
            catchBlock.body().invoke(task, "set").arg(code.ref(Result.class).staticInvoke("create").arg(res).invoke("setThrowable").arg(x));
            catchBlock.body()._return();
            JInvocation invoc = JExpr.invoke("post").arg(ObjectBindingModel.getExecutor(m)).arg(lambda);
            block.add(invoc);
            block = lambda.body();
            ctr++;

        }
        block.addSingleLineComment("the end of the call chain: tell the task that we are done");
        block.invoke(task, "set").arg(code.ref(Result.class).staticInvoke("create").arg(res));
    }

    /**
     * Example:
     * <pre>
     * protected void onPreDestroy(SettableTask<Result<Void>> task, @Nullable CartUIS cartUIS){
     *   post("hallo", () -> {
     *    delegateBlub0(cartUIS);
     *    post("main", () -> {
     *     delegateBlub1(cartUIS);
     *     onPostExecuteDone();
     *    });
     *   });
     * }
     * </pre>
     *
     * @param methods
     * @param code
     * @param binding
     */
    private void generatePreDestroy(AbstractJClass param, List<Method> methods, JCodeModel code, JDefinedClass binding) {
        JMethod override = binding.method(JMod.PROTECTED, void.class, "onPreDestroy");
        JVar task = override.param(code.ref(SettableTask.class).narrow(code.ref(Result.class).narrow(param)), "task");
        JVar res = override.param(param, "res");
        override.annotate(Override.class);

        JBlock block = override.body();

        if (methods.isEmpty()) {
            block.add(JExpr._super().invoke("onPreDestroy").arg(task).arg(res));
            return;
        }

        int ctr = 0;
        for (Method m : methods) {
            JLambda lambda = new JLambda();
            String name = generateMethodCall(param, m, code, binding);
            JTryBlock tryBlock = lambda.body()._try();
            tryBlock.body().add(JExpr.invoke(name).arg(res));
            JCatchBlock catchBlock = tryBlock._catch(code.ref(Throwable.class));
            JVar x = catchBlock.param("e" + ctr);
            catchBlock.body().invoke(task, "set").arg(code.ref(Result.class).staticInvoke("create").arg(res).invoke("setThrowable").arg(x));
            catchBlock.body()._return();
            JInvocation invoc = JExpr.invoke("post").arg(ObjectBindingModel.getExecutor(m)).arg(lambda);
            block.add(invoc);
            block = lambda.body();
            ctr++;

        }
        block.addSingleLineComment("the end of the call chain: tell the task that we are done");
        block.invoke(task, "set").arg(code.ref(Result.class).staticInvoke("create").arg(res));
    }

    private String generateMethodCall(AbstractJClass param, Method m, JCodeModel code, JDefinedClass binding) {

        String methodName = "invoke" + Strings.startUpperCase(m.getName());
        int ctr = 1;
        while (binding.getMethod(methodName, new AbstractJType[]{param}) != null) {
            methodName = "invoke" + Strings.startUpperCase(m.getName()) + ctr;
            ctr++;
        }
        JMethod invoke = binding.method(JMod.PRIVATE, void.class, methodName);
        invoke.javadoc().add("See {@link " + m.asJavadocAnchor() + "}");
        invoke._throws(Exception.class);
        JVar p = invoke.param(param, "obj");

        if (m.isPublic() || (m.isDeclared() && m.isDefault())) {
            //yeah: we can avoid reflection and link directly to the call
            invoke.body().add(p.invoke(m.getName()));
        } else {
            //meh: we need reflection
            JFieldVar refMethod = binding.field(JMod.PRIVATE, java.lang.reflect.Method.class, "method" + Strings.startUpperCase(methodName));
            refMethod.javadoc().add("declared at {@link " + m.asJavadocAnchor() + "}");
            JBlock initBlock = invoke.body()._if(refMethod.eqNull())._then().synchronizedBlock(JExpr._this()).body()._if(refMethod.eqNull())._then();
            initBlock.assign(refMethod, code.ref(Reflection.class).staticInvoke("getMethod").arg(param.dotclass()).arg(m.getName()).arg(JExpr.newArray(code.ref(Class.class), 0)));
            initBlock.add(refMethod.invoke("setAccessible").arg(true));
            invoke.body().invoke(refMethod, "invoke").arg(p);
        }


        return methodName;
    }

    private void sortMethodCalls(List<Method> methods) {
        Collections.sort(methods, (a, b) -> {
            Annotation aPriority = a.getAnnotation(Priority.class);
            int aPrio = 0;
            if (aPriority != null) {
                Long p = aPriority.getLong("");
                if (p != null) {
                    aPrio = p.intValue();
                }
            }
            Annotation bPriority = b.getAnnotation(Priority.class);
            int bPrio = 0;
            if (bPriority != null) {
                Long p = bPriority.getLong("");
                if (p != null) {
                    bPrio = p.intValue();
                }
            }
            return -Integer.compare(aPrio, bPrio);
        });
    }


    static class ObjectBindingModel {
        FullQualifiedName bean;
        List<Method> postConstruct = new ArrayList<>();
        List<Method> preDestroy = new ArrayList<>();
        List<Field> fields = new ArrayList<>();
        List<Field> constructorFields = new ArrayList<>();
        @Nullable
        Constructor constructor = null;

        static String getExecutor(Method method) {
            Annotation exec = method.getAnnotation(Execute.class);
            if (exec == null) {
                return Container.NAME_MAIN_HANDLER;
            }
            return exec.getString("");
        }
    }

    static class OnExecuteMethod {
        JMethod onExecute;
        JVar varBean;
        JInvocation newBeanStatement;
    }

}
