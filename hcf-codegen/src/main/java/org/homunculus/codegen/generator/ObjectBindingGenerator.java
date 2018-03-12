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

//        //pick all available fields, including super classes
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

        //the constructor is build from the original constructor and special annotated fields
        createTypeSafeBindingConstructor(project.getCodeModel(), binding, objectBindingModel);

//
//        //add the constructor for this binding
//        JMethod con = binding.constructor(JMod.PUBLIC);
//        Set<Parameter> dynamicConstructorParams = new HashSet<>();
//        Set<Parameter> fieldConstructorParams = new HashSet<>();
//        //first add the actual constructor parameters
//        if (bestConstructor != null) {
//            NEXT_PARAM:
//            for (Parameter p : bestConstructor.getParameters()) {
//                AbstractJClass pType = project.getCodeModel().ref(file.getFullQualifiedName(p.getType().asString()));
//                for (AnnotationExpr annotation : p.getAnnotations()) {
//                    String aFqn = file.getFullQualifiedName(annotation.getNameAsString());
//                    if (aFqn.equals(FactoryParam.class.getName())) {
//
//                        JVar pVar = con.param(pType, p.getNameAsString());
//
//                        //the field
//                        JFieldVar thisVar = binding.field(JMod.PRIVATE, pType, p.getNameAsString());
//                        thisVar.javadoc().add("introduced by @FactoryParam from within the constructor");
//
//                        //the setting
//                        con.body().assign(JExpr._this().ref(thisVar), pVar);
//
//                        fieldConstructorParams.add(p);
//                        continue NEXT_PARAM;
//                    }
//                }
//                dynamicConstructorParams.add(p);
//            }
//        }
//
//        //second add all injectable fields which are marked as factory param
//        for (FieldLegacy field : fields) {
//            //the static field
////            if (!field.isFactoryParam) {
//            JFieldVar thisStaticVar = binding.field(JMod.PRIVATE | JMod.STATIC, java.lang.reflect.Field.class, "field" + project.startUpperCase(field.getFieldName()));
//            thisStaticVar.javadoc().add("a static reflection field cache to support private member injection");
////            }
//        }
//
//        for (FieldLegacy field : fields) {
//            if (field.isFactoryParam) {
//                AbstractJClass pType = project.getCodeModel().ref(file.getFullQualifiedName(field.dec.getVariables().get(0).getType().asString()));
//                JVar pVar = con.param(pType, field.getFieldName());
//
//                //the field
//                JFieldVar thisVar = binding.field(JMod.PRIVATE, pType, field.getFieldName());
//                if (field.isNullable) {
//                    thisVar.annotate(Nullable.class);
//                    pVar.annotate(Nullable.class);
//                }
//                thisVar.javadoc().add("introduced by @FactoryParam from a field annotation");
//
//                //the setting
//                con.body().assign(JExpr._this().ref(thisVar), pVar);
//            }
//        }
//
//        //the static reflection field cache to make performance pretty perfect - it is usually as fast as directly setting the value
//        JMethod initStatic = binding.method(JMod.PROTECTED, void.class, "initStatic");
//        initStatic.annotate(project.getCodeModel().ref(Override.class));
//        JVar varFields = initStatic.body().decl(project.getCodeModel().ref(Map.class).narrow(String.class, java.lang.reflect.Field.class), "_fields");
//        initStatic.body().assign(varFields, project.getCodeModel().ref(Reflection.class).staticInvoke("getFieldsMap").arg(JExpr.dotclass(project.getCodeModel().ref(file.getFullQualifiedNamePrimaryClassName()))));
//        for (FieldLegacy field : fields) {
//            if (field.isFactoryParam) {
//                initStatic.body().assign(binding.fields().get("field" + project.startUpperCase(field.getFieldName())), varFields.invoke("get").arg(field.getFieldName()));
//            }
//
//        }
//
//        //override onBind for the scope
//        JMethod onBind = binding.method(JMod.PROTECTED, void.class, "onBind");
//        JVar dstVar = onBind.param(Scope.class, "dst");
//        onBind.annotate(project.getCodeModel().ref(Override.class));
//        for (FieldLegacy field : fields) {
//            if (field.isFactoryParam) {
//                onBind.body().add(dstVar.invoke("put").arg(field.getFieldName()).arg(binding.fields().get(field.getFieldName())));
//            }
//        }
//
//        for (Parameter p : fieldConstructorParams) {
//            onBind.body().add(dstVar.invoke("put").arg(p.getNameAsString()).arg(binding.fields().get(p.getNameAsString())));
//        }
//
//
////        override onExecute which performs the actual calling
//        JMethod onExecute = binding.method(JMod.PROTECTED, project.getCodeModel().ref(file.getFullQualifiedNamePrimaryClassName()), "onExecute")._throws(Exception.class);
//        onExecute.annotate(project.getCodeModel().ref(Override.class));
//        JVar bean = onExecute.body().decl(project.getCodeModel().ref(file.getFullQualifiedNamePrimaryClassName()), "_bean");
//        JInvocation _new = JExpr._new(project.getCodeModel().ref(file.getFullQualifiedNamePrimaryClassName()));
//        //create the constructor params
//        if (bestConstructor != null) {
//            for (Parameter p : bestConstructor.getParameters()) {
//                if (dynamicConstructorParams.contains(p)) {
//                    AbstractJClass pType = project.getCodeModel().ref(file.getFullQualifiedName(p.getType().asString()));
//                    _new.arg(JExpr.invoke("get").arg(pType.dotclass()));
//                } else if (fieldConstructorParams.contains(p)) {
//                    _new.arg(binding.fields().get(p.getNameAsString()));
//                } else {
//                    throw new InternalError();
//                }
//            }
//
//        }
//        onExecute.body().assign(bean, _new);
//
//        for (FieldLegacy field : fields) {
//            //fieldSomeString.set(obj, get("someString", String.class)); => better fieldSomeString.set(obj, get(fieldSomeString))
//            JVar staticField = binding.fields().get("field" + project.startUpperCase(field.getFieldName()));
//            AbstractJClass pType = project.getCodeModel().ref(file.getFullQualifiedName(field.dec.getVariables().get(0).getType().asString()));
//            JInvocation selfGet = JExpr.invoke("get").arg(staticField);
//            onExecute.body().invoke(staticField, "set").arg(bean).arg(selfGet);
//        }
//
//
//        onExecute.body()._return(bean);


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

    private void createTypeSafeBindingConstructor(JCodeModel code, JDefinedClass binding, ObjectBindingModel model) {
        JMethod c = binding.constructor(JMod.PUBLIC);
        if (model.constructor != null) {
            for (Parameter p : model.constructor.getParameters()) {
                AbstractJClass cl = code.ref(p.getType().toString());
                JVar paramVar = c.param(cl, p.getName());
                JFieldVar fieldVar = binding.field(JMod.PRIVATE, cl, p.getName());
                fieldVar.javadoc().add("required by {@link " + model.constructor.asJavadocAnchor() + "}");
                c.body().assign(JExpr.refthis(fieldVar), paramVar);
            }
        }

        for (Field field : model.constructorFields) {
            AbstractJClass cl = code.ref(field.getType().getFullQualifiedName().toString());
            JVar paramVar = c.param(cl, field.getName());
            JFieldVar fieldVar = binding.field(JMod.PRIVATE, cl, field.getName());
            c.body().assign(JExpr.refthis(fieldVar), paramVar);
        }


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
        if (methods.isEmpty()) {
            return;
        }

        JBlock block = override.body();
        JBlock errExit = block._if(exception.neNull())._then();
        errExit.invoke(task, "set").arg(code.ref(Result.class).staticInvoke("create").arg(res).invoke("setThrowable").arg(exception));
        errExit._return();

        if (methods.isEmpty()) {
            block.add(JExpr._super().invoke("onPostExecute").arg(task).arg(res));
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
        invoke._throws(Exception.class);
        JVar p = invoke.param(param, "obj");

        if (m.isPublic() || (m.isDeclared() && m.isDefault())) {
            //yeah: we can avoid reflection and link directly to the call
            invoke.body().add(p.invoke(m.getName()));
        } else {
            //meh: we need reflection
            JVar refMethod = binding.field(JMod.PRIVATE, java.lang.reflect.Method.class, "method" + Strings.startUpperCase(methodName));
            JBlock initBlock = invoke.body()._if(refMethod.eqNull())._then().synchronizedBlock(JExpr._this()).body()._if(refMethod.eqNull())._then();
            initBlock.assign(refMethod, code.ref(Reflection.class).staticInvoke("getMethod").arg(param.dotclass()).arg(m.getName()).arg(JExpr.newArray(code.ref(Class.class), 0)));
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

//    /**
//     * Tries to resolve the actual value of an {@link Named} annotation
//     *
//     * @param annotations
//     * @return
//     */
//    @Nullable
//    static String resolveName(SrcFile src, NodeList<AnnotationExpr> annotations) {
//        String beanName = null;
//        for (AnnotationExpr annotation : annotations) {
//            beanName = resolveName(src, annotation);
//            if (beanName != null) {
//                return beanName;
//            }
//        }
//        return null;
//    }
//
//    static String resolveName(SrcFile src, AnnotationExpr annotation) {
//        String beanName = null;
//        String aFqn = src.getFullQualifiedName(annotation.getNameAsString());
//        if (aFqn.equals(Named.class.getName())) {
//            if (annotation instanceof SingleMemberAnnotationExpr) {
//                SingleMemberAnnotationExpr expr = ((SingleMemberAnnotationExpr) annotation);
//                if (expr.getMemberValue() instanceof StringLiteralExpr) {
//                    beanName = ((StringLiteralExpr) expr.getMemberValue()).getValue();
//                    return beanName;
//                } else if (expr.getMemberValue() instanceof NameExpr) {
//                    NameExpr nameExpr = ((NameExpr) expr.getMemberValue());
//                    String value = resolveStaticFieldValue(src, nameExpr.getNameAsString());
//                    if (value == null) {
//                        LoggerFactory.getLogger(ObjectBindingGenerator.class).warn("constant evaluation not supported: {}", nameExpr);
//                    } else {
//                        beanName = value;
//                        return beanName;
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//    static void collectInjectableFields(GenProject project, SrcFile src, ClassOrInterfaceDeclaration dec, List<FieldLegacy> dst) {
//        if (dec.getExtendedTypes().size() > 0) {
//            String fqnSuper = src.getPackageName() + "." + dec.getExtendedTypes().get(0).getNameAsString();
//            SrcFile superFile = project.findSourceFileForType(fqnSuper);
//            if (superFile != null) {
//                LoggerFactory.getLogger(ObjectBindingGenerator.class).warn("following super class: {}", fqnSuper);
//                collectInjectableFields(project, superFile, superFile.getUnit().getClassByName(superFile.getPrimaryClassName()).get(), dst);
//            } else {
//                LoggerFactory.getLogger(ObjectBindingGenerator.class).warn("unable to resolve source code for: {}", fqnSuper);
//            }
//        }
//        for (FieldDeclaration field : dec.getFields()) {
//            FieldLegacy tmp = new FieldLegacy(src, field);
//            for (AnnotationExpr annotation : field.getAnnotations()) {
//                String aFqn = src.getFullQualifiedName(annotation.getNameAsString());
//                if (aFqn.equals(Inject.class.getName()) || aFqn.equals(Autowired.class.getName())) {
//                    tmp.isInjectable = true;
//                    continue;
//                }
//                if (aFqn.equals(FactoryParam.class.getName())) {
//                    tmp.isFactoryParam = true;
//                }
//                if (aFqn.equals(Nullable.class.getName())) {
//                    tmp.isNullable = true;
//                }
//
//                if (aFqn.equals(Named.class.getName())) {
//                    String t = resolveName(src, annotation);
//                    if (t != null) {
//                        tmp.alternateName = t;
//                    }
//                }
//                if (field.isFinal()) {
//                    throw new LintException("field '" + tmp.getFieldName() + "' must not be final", src, field.getRange().get());
//                }
//            }
//            if (tmp.isInjectable) {
//                dst.add(tmp);
//            }
//        }
//    }
//
//    @Nullable
//    private static String resolveStaticFieldValue(SrcFile src, String fieldName) {
//        for (FieldDeclaration f : src.getUnit().getClassByName(src.getPrimaryClassName()).get().getFields()) {
//            if (f.getVariables().size() > 0) {
//                VariableDeclarator var = f.getVariables().get(0);
//                if (var.getNameAsString().equals(fieldName)) {
//                    if (var.getInitializer().isPresent()) {
//                        if (var.getInitializer().get() instanceof StringLiteralExpr) {
//                            return var.getInitializer().get().asStringLiteralExpr().asString();
//                        }
//
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//
//    static class FieldLegacy {
//        final SrcFile file;
//        final FieldDeclaration dec;
//        boolean isInjectable = false;
//        boolean isFactoryParam = false;
//        boolean isNullable = false;
//        String alternateName = null;
//
//        public FieldLegacy(SrcFile file, FieldDeclaration dec) {
//            this.file = file;
//            this.dec = dec;
//        }
//
//        public String getFieldName() {
//            if (alternateName == null) {
//                return dec.getVariables().get(0).getNameAsString();
//            } else {
//                return alternateName;
//            }
//        }
//    }


    static class ObjectBindingModel {
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


}
