package org.homunculus.codegen.generator;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JTypeVar;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.generator.PreprocessDiscoverBeans.DiscoveryKind;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Constructor;
import org.homunculus.codegen.parse.Field;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Parameter;
import org.homunculus.codegen.parse.Resolver;
import org.homunculus.codegen.parse.Strings;
import org.homunculusframework.factory.container.ModelAndView;
import org.homunculusframework.factory.container.ObjectBinding;
import org.homunculusframework.factory.flavor.hcf.Bind;
import org.homunculusframework.factory.scope.LifecycleOwner;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Ref;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Created by Torben Schinke on 16.03.18.
 */

public class GenerateBindables implements Generator {
    private final static FullQualifiedName HCF_ANDROID_RES = new FullQualifiedName("org.homunculus.android.flavor.Resource");
    private final static FullQualifiedName ANDROID_VIEW = new FullQualifiedName(View.class);
    private final static FullQualifiedName STRING = new FullQualifiedName(String.class);
    private final static FullQualifiedName ANDROID_DRAWABLE = new FullQualifiedName(Drawable.class);
    private final static FullQualifiedName ANDROID_BITMAP = new FullQualifiedName(Bitmap.class);

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

            PreSolved preSolved = new PreSolved();
            preSolved.constructor = shortestConstructor;
            preSolved.allFields = project.getResolver().getFields(bean);
            preSolved.injectParams = new ArrayList<>();
            for (Field field : preSolved.allFields) {
                if (field.getAnnotation(Bind.class) != null) {
                    preSolved.bindParams.add(field);
                    continue;
                }
                Annotation androidResource = field.getAnnotation(HCF_ANDROID_RES);
                if (androidResource != null) {
                    FullQualifiedName fqn = androidResource.getFullQualifiedName();
                    if (fqn == null) {
                        throw androidResource.newLintException("unsupported constant declaration");
                    }
                    preSolved.androidResource.add(field);
                    continue;
                }
                if (field.getAnnotation(Inject.class) != null) {
                    preSolved.injectParams.add(field);
                }
            }
            createBindBean(project.getResolver(), code, bean, binder, preSolved);

            binder._extends(code.ref(ModelAndView.class).narrow(preSolved.extendClassNarrows));
        }
    }

    private void createBindBean(Resolver resolver, JCodeModel code, FullQualifiedName bindable, JDefinedClass binder, PreSolved preSolved) throws Exception {
        JMethod creator = binder.constructor(JMod.PUBLIC);
        //repeat the actual constructor parameters, if noted with bind
        if (preSolved.constructor != null) {
            for (Parameter p : preSolved.constructor.getParameters()) {
                if (p.getAnnotation(Bind.class) != null) {
                    JVar cParam = creator.param(code.ref(p.getType().toString()), p.getName());
                    JFieldVar field = binder.field(JMod.PRIVATE | JMod.FINAL, cParam.type(), cParam.name());
                    creator.body().add(JExpr._this().ref(field).assign(cParam));
                }
            }
        }

        //otherwise add all fields which are bound
        for (Field bindParam : preSolved.bindParams) {
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
        AbstractJClass bindableScopeParent = code.ref(bindableScope.constructors().next().listParams()[0].type().fullName());
        JDefinedClass parentScope = code._getClass(bindableScopeParent.fullName());
        AbstractJType bindableType = code.ref(bindable.toString());

        //e.g. UISA bindable = new UISA(...)
        JMethod createBindable = binder.method(JMod.PUBLIC, bindableScope, "create");
        createBindable._throws(Exception.class);
        createBindable.annotate(Override.class);
        preSolved.extendClassNarrows.add(bindableScope);
        preSolved.extendClassNarrows.add(bindableScopeParent.narrowAny());

        JVar varParentScope = createBindable.param(bindableScopeParent, "scope");
        JInvocation invocCtr = JExpr._new(bindableType);
        JVar bean = createBindable.body().decl(bindableType, "bindable", invocCtr);
        JInvocation beanScopeCtr = JExpr._new(bindableScope).arg(varParentScope).arg(bean);
        JVar beanScope = createBindable.body().decl(bindableScope, "bindableScope", beanScopeCtr);

        Ref<Boolean> hasOwnership = new Ref<>(false);
        //satisfy the actual constructor of the bindable
        if (preSolved.constructor != null) {
            for (Parameter p : preSolved.constructor.getParameters()) {
                if (p.getAnnotation(Bind.class) != null) {
                    invocCtr.arg(binder.fields().get(p.getName()));
                } else {
                    invocCtr.arg(resolveDependencyFromScope(resolver, code, beanScope, parentScope, varParentScope, code.ref(p.getType().toString()), hasOwnership));
                    //TODO potential ownership destruction violation
                }
            }
        }

        //inject the bind values from field
        for (Field bindParam : preSolved.bindParams) {
            JFieldVar field = binder.fields().get(bindParam.getName());
            createBindable.body().add(bean.ref(bindParam.getName()).assign(field));
        }

        //inject the other values from scope
        for (Field injectParam : preSolved.injectParams) {
            try {
                IJExpression invoc = resolveDependencyFromScope(resolver, code, beanScope, parentScope, varParentScope, code.ref(injectParam.getType().getFullQualifiedName().toString()), hasOwnership);
                createBindable.body().add(bean.ref(injectParam.getName()).assign(invoc));
            } catch (Panic e) {
                e.printStackTrace();
                throw injectParam.newLintException("failed to get injection source. This is caused by using an abstract or interface type without providing an instance of @ScopeElement");
            }
        }

        //inject the android resources
        for (Field androidResource : preSolved.androidResource) {
            FullQualifiedName fqn = androidResource.getAnnotation(HCF_ANDROID_RES).getConstant("");
            IJExpression getContext = resolveDependencyFromScope(resolver, code, beanScope, parentScope, varParentScope, code.ref(Context.class), hasOwnership);

            IJExpression staticRef = JExpr.direct(fqn.toString());
            FullQualifiedName fieldFqn = androidResource.getType().getFullQualifiedName();
            if (fieldFqn.equals(STRING)) {
                createBindable.body().add(bean.ref(androidResource.getName()).assign(getContext.invoke("getString").arg(staticRef)));
            } else if (fieldFqn.equals(ANDROID_VIEW)) {
                //e.g. LayoutInflater.from(scope.getConceptActivity()).inflate(1,null);
                AbstractJClass layoutInflater = code.ref(LayoutInflater.class);
                createBindable.body().add(bean.ref(androidResource.getName()).assign(layoutInflater.staticInvoke("from").arg(getContext).invoke("inflate").arg(staticRef).arg(JExpr._null())));
            } else if (fieldFqn.equals(ANDROID_DRAWABLE)) {
                //e.g. getContext().getDrawable(1);
                createBindable.body().add(bean.ref(androidResource.getName()).assign(getContext.invoke("getResources").invoke("getDrawable").arg(staticRef)));
            } else if (fieldFqn.equals(ANDROID_BITMAP)) {
                //e.g. BitmapFactory.decodeResource(context.getResources(), resource.value()));
                AbstractJClass bitmapFactory = code.ref(BitmapFactory.class);
                createBindable.body().add(bean.ref(androidResource.getName()).assign(bitmapFactory.staticInvoke("decodeResource").arg(getContext.invoke("getResources")).arg(staticRef)));
            } else {
                throw androidResource.getAnnotation(HCF_ANDROID_RES).newLintException("cannot provide android resource into " + androidResource.getType().getFullQualifiedName());
            }
        }

        //return the new Scope
        createBindable.body()._return(beanScope);
    }


    private IJExpression resolveDependencyFromScope(Resolver resolver, JCodeModel code, JVar beanScope, JDefinedClass parentScope, JVar varParentScope, AbstractJType type, Ref<Boolean> hasOwnership) throws Exception {
        if (resolver.isInstanceOf(new FullQualifiedName(type.fullName()), new FullQualifiedName(Scope.class))) {
            return beanScope;
        }
        if (resolver.isInstanceOf(new FullQualifiedName(type.fullName()), new FullQualifiedName(LifecycleOwner.class))) {
            return beanScope;
        }
        hasOwnership.set(false);
        JMethod matchingMethod = resolveMatchingMethod(resolver, parentScope, type);
        if (matchingMethod != null) {
            hasOwnership.set(true);
            return varParentScope.invoke(matchingMethod);
        }

        //ups, was unable to resolve from scope, try it's parent (this is currently always the application, we do not have another level yet)
        for (JMethod method : parentScope.methods()) {
            if (method.name().equals("getParent")) {
                hasOwnership.set(false);
                JDefinedClass parentParentScope = code._getClass(method.type().fullName());
                JMethod parentMatchingMethod = resolveMatchingMethod(resolver, parentParentScope, type);
                if (parentMatchingMethod != null) {
                    return varParentScope.invoke(method).invoke(parentMatchingMethod);
                }
                break;
            }
        }

        //well no such dependency, so let's try to create an instance from scratch
        JInvocation constructorCall = createConstructorCall(resolver, code, beanScope, parentScope, varParentScope, type, hasOwnership);
        hasOwnership.set(true);
        return constructorCall;
    }

    private JMethod resolveMatchingMethod(Resolver resolver, JDefinedClass parentScope, AbstractJType type) {
        FullQualifiedName targetType = new FullQualifiedName(type.fullName());
        for (JMethod method : parentScope.methods()) {
            if (method.name().startsWith("get")) {

                //a candidate, factories start with 'create'*
                FullQualifiedName methodType = resolveTypeParam(parentScope, method);
                if (resolver.isInstanceOf(methodType, targetType)) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * e.g. resolves a generic 'T' of a method to a bound on the class like 'Activity', otherwise just returns the method type
     */
    private static FullQualifiedName resolveTypeParam(JDefinedClass cl, JMethod method) {
        for (JTypeVar typeVar : cl.typeParams()) {
            if (method.type().fullName().equals(typeVar.fullName())) {
                return new FullQualifiedName(typeVar.bounds().iterator().next().fullName());
            }
        }
        return new FullQualifiedName(method.type().fullName());
    }

    private JInvocation createConstructorCall(Resolver resolver, JCodeModel code, JVar beanScope, JDefinedClass parentScope, JVar varParentScope, AbstractJType type, Ref<Boolean> hasOwnership) throws Exception {
        //is there a scope binder?
        FullQualifiedName fqnType = new FullQualifiedName(type.fullName());
        FullQualifiedName binder = new FullQualifiedName(fqnType.getPackageName() + ".Bind" + fqnType.getSimpleName());
        if (code._getClass(binder.toString()) != null) {
            //we have to create a binder for it, very ugly
            return resolveDependencyFromScope(resolver, code, beanScope, parentScope, varParentScope, code.ref(binder.toString()), hasOwnership).invoke("create").arg(varParentScope).invoke("get" + Strings.startUpperCase(fqnType.getSimpleName()));
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
                newCall.arg(resolveDependencyFromScope(resolver, code, beanScope, parentScope, varParentScope, code.ref(p.getType().toString()), hasOwnership));
            }
            return newCall;
        } else {
            //this is the "Bind"* case
            JDefinedClass definedClass = code._getClass(type.fullName());
            if (definedClass == null) {
                throw new Panic("no such generated class: " + type.fullName());
            }
            if (!definedClass.constructors().hasNext()) {
                throw new RuntimeException("cannot find any constructor for " + definedClass);
            }
            JMethod method = definedClass.constructors().next();
            JInvocation newCall = JExpr._new(type);
            for (JVar p : method.params()) {
                newCall.arg(resolveDependencyFromScope(resolver, code, beanScope, parentScope, varParentScope, p.type(), hasOwnership));
            }
            return newCall;
        }
    }

    private static class PreSolved {
        @Nullable
        Constructor constructor;
        List<Field> allFields = new ArrayList<>();
        List<Field> bindParams = new ArrayList<>();
        List<Field> injectParams = new ArrayList<>();
        List<Field> androidResource = new ArrayList<>();
        List<Field> ownedDestroyableElements = new ArrayList<>();
        List<AbstractJClass> extendClassNarrows = new ArrayList<>();
    }
}
