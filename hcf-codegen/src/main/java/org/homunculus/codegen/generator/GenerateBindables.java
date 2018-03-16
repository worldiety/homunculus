package org.homunculus.codegen.generator;


import com.helger.jcodemodel.AbstractJType;
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
import org.homunculus.codegen.parse.Constructor;
import org.homunculus.codegen.parse.Field;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Method;
import org.homunculus.codegen.parse.Parameter;
import org.homunculus.codegen.parse.Resolver;
import org.homunculus.codegen.parse.Strings;
import org.homunculusframework.factory.flavor.hcf.Bind;
import org.homunculusframework.lang.Panic;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Torben Schinke on 16.03.18.
 */

public class GenerateBindables implements Generator {
    @Override
    public void generate(GenProject project) throws Exception {
        JCodeModel code = project.getCodeModel();
        for (FullQualifiedName bean : project.getDiscoveredKinds().get(DiscoveryKind.BIND)) {
            JDefinedClass binder = code._class(JMod.PUBLIC, bean.getPackageName() + ".Bind" + bean.getSimpleName());

            Constructor shortestConstructor = null;
            for (Constructor ctr : project.getResolver().getConstructors(bean)) {
                if (shortestConstructor == null || ctr.getParameters().size() < ctr.getParameters().size()) {
                    shortestConstructor = ctr;
                }
            }

            List<Field> allFields = project.getResolver().getFields(bean);
            List<Field> bindParams = new ArrayList<>();
            List<Field> injectParams = new ArrayList<>();
            for (Field field : allFields) {
                if (field.getAnnotation(Bind.class) != null) {
                    bindParams.add(field);
                    continue;
                }
                if (field.getAnnotation(Inject.class) != null) {
                    injectParams.add(field);
                }
            }
            createBindBean(project.getResolver(), code, bean, binder, shortestConstructor, bindParams, injectParams);
        }
    }

    private void createBindBean(Resolver resolver, JCodeModel code, FullQualifiedName bindable, JDefinedClass binder, @Nullable Constructor constructor, List<Field> bindParams, List<Field> injectParams) throws Exception {
        JMethod creator = binder.constructor(JMod.PUBLIC);
        //repeat the actual constructor parameters, if noted with bind
        if (constructor != null) {
            for (Parameter p : constructor.getParameters()) {
                if (p.getAnnotation(Bind.class) != null) {
                    JVar cParam = creator.param(code.ref(p.getType().toString()), p.getName());
                    JFieldVar field = binder.field(JMod.PRIVATE | JMod.FINAL, cParam.type(), cParam.name());
                    creator.body().add(JExpr._this().ref(field).assign(cParam));
                }
            }
        }

        //otherwise add all fields which are bound
        for (Field bindParam : bindParams) {
            JVar cParam = creator.param(code.ref(bindParam.getType().getFullQualifiedName().toString()), bindParam.getName());
            JFieldVar field = binder.field(JMod.PRIVATE | JMod.FINAL, cParam.type(), cParam.name());
            creator.body().add(JExpr._this().ref(field).assign(cParam));
        }


        //add the execute method with the correct parent scope
        JDefinedClass bindableScope = code._getClass(bindable + "Scope");
        if (bindableScope == null) {
            throw new Panic("GenerateScopes must run before");
        }

        //this is harcoded know-how - this is always the ActivityScope
        AbstractJType bindableScopeParent = bindableScope.constructors().next().listParams()[0].type();
        JDefinedClass parentScope = code._getClass(bindableScopeParent.fullName());
        AbstractJType bindableType = code.ref(bindable.toString());

        //e.g. UISA bindable = new UISA(...)
        JMethod createBindable = binder.method(JMod.PUBLIC, bindableScope, "create");
        JVar varParentScope = createBindable.param(bindableScopeParent, "scope");
        JInvocation invocCtr = JExpr._new(bindableType);
        JVar bean = createBindable.body().decl(bindableType, "bindable", invocCtr);

        //satisfy the actual constructor of the bindable
        if (constructor != null) {
            for (Parameter p : constructor.getParameters()) {
                if (p.getAnnotation(Bind.class) != null) {
                    invocCtr.arg(binder.fields().get(p.getName()));
                } else {
                    invocCtr.arg(resolveDependencyFromScope(resolver, code, parentScope, varParentScope, code.ref(p.getType().toString())));
                }
            }
        }

        //inject the bind values from field
        for (Field bindParam : bindParams) {
            JFieldVar field = binder.fields().get(bindParam.getName());
            createBindable.body().add(bean.ref(bindParam.getName()).assign(field));
        }

        //inject the other values from scope
        for (Field injectParam : injectParams) {
            System.out.println("inject param type = " + injectParam.getType().getFullQualifiedName());
            JInvocation invoc = resolveDependencyFromScope(resolver, code, parentScope, varParentScope, code.ref(injectParam.getType().getFullQualifiedName().toString()));
            createBindable.body().add(bean.ref(injectParam.getName()).assign(invoc));
        }


        //call the PostConstruct methods
        for (Method method : resolver.getMethods(bindable)) {
            if (method.getAnnotation(PostConstruct.class) != null) {
                createBindable.body().add(bean.invoke(method.getName()));
            }
        }

        //return the new Scope
        createBindable.body()._return(JExpr._new(bindableScope).arg(varParentScope).arg(bean));
    }

    private JInvocation resolveDependencyFromScope(Resolver resolver, JCodeModel code, JDefinedClass parentScope, JVar varParentScope, AbstractJType type) throws Exception {

        JMethod matchingMethod = resolveMatchingMethod(resolver, parentScope, type);
        if (matchingMethod != null) {
            return varParentScope.invoke(matchingMethod);
        }

        //ups, was unable to resolve from scope, try it's parent (this is currently always the application, we do not have another level yet)
        for (JMethod method : parentScope.methods()) {
            if (method.name().equals("getParent")) {
                JDefinedClass parentParentScope = code._getClass(method.type().fullName());
                JMethod parentMatchingMethod = resolveMatchingMethod(resolver, parentParentScope, type);
                if (parentMatchingMethod != null) {
                    return varParentScope.invoke(method).invoke(parentMatchingMethod);
                }
                break;
            }
        }

        //well no such dependency, so let's try to create an instance from scratch
        return createConstructorCall(resolver, code, parentScope, varParentScope, type);
    }

    private JMethod resolveMatchingMethod(Resolver resolver, JDefinedClass parentScope, AbstractJType type) {
        FullQualifiedName targetType = new FullQualifiedName(type.fullName());
        for (JMethod method : parentScope.methods()) {
            if (method.name().startsWith("get")) {
                //a candidate, factories start with 'create'*
                FullQualifiedName methodType = new FullQualifiedName(method.type().fullName());
                if (resolver.isInstanceOf(methodType, targetType)) {
                    return method;
                }
            }
        }
        return null;
    }

    private JInvocation createConstructorCall(Resolver resolver, JCodeModel code, JDefinedClass parentScope, JVar varParentScope, AbstractJType type) throws Exception {
        //is there a scope binder?
        FullQualifiedName fqnType = new FullQualifiedName(type.fullName());
        FullQualifiedName binder = new FullQualifiedName(fqnType.getPackageName() + ".Bind" + fqnType.getSimpleName());
        if (code._getClass(binder.toString()) != null) {
            //we have to create a binder for it, very ugly
            return resolveDependencyFromScope(resolver, code, parentScope, varParentScope, code.ref(binder.toString())).invoke("create").arg(varParentScope).invoke("get" + Strings.startUpperCase(fqnType.getSimpleName()));
        }

        if (resolver.has(new FullQualifiedName(type.fullName()))) {

            Constructor shortestConstructor = null;
            for (Constructor ctr : resolver.getConstructors(new FullQualifiedName(type.fullName()))) {
                if (shortestConstructor == null || ctr.getParameters().size() < ctr.getParameters().size()) {
                    shortestConstructor = ctr;
                }
            }

            //the easy one
            if (shortestConstructor == null || shortestConstructor.getParameters().isEmpty()) {
                return JExpr._new(type);
            }

            //the hard one
            JInvocation newCall = JExpr._new(type);
            for (Parameter p : shortestConstructor.getParameters()) {
                newCall.arg(resolveDependencyFromScope(resolver, code, parentScope, varParentScope, code.ref(p.getType().toString())));
            }
            return newCall;
        } else {
            //this is the "Bind"* case
            JDefinedClass definedClass = code._getClass(type.fullName());
            JMethod method = definedClass.constructors().next();
            JInvocation newCall = JExpr._new(type);
            for (JVar p : method.params()) {
                newCall.arg(resolveDependencyFromScope(resolver, code, parentScope, varParentScope, p.type()));
            }
            return newCall;
        }
    }
}
