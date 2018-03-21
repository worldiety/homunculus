package org.homunculus.codegen.generator;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JAssignment;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JDirectClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JSynchronizedBlock;
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
import org.homunculusframework.factory.flavor.hcf.ScopeElement;
import org.homunculusframework.factory.scope.AbsScope;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.factory.scope.ContextScope;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 * Creates individual scopes for Applications, Activities and Bind-ables (== User interface states). These guys form a
 * hierarchy with some hardcoded behavior, e.g.
 * <ul>
 * <li>Application scopes contain the singletons</li>
 * <li>Activity scopes refer to the application (assuming only one) => TODO this means you cannot use the same Activity scope with different applications</li>
 * <li>Bindable scopes refer to the activity scope (assuming only one) as their parent. A common superset of methods for all activities is expected. </li>
 * <li>Each base class can provide factory methods for their scope by using @ScopeElement on their public or package private members</li>
 * </ul>
 * <p>
 * Created by Torben Schinke on 16.03.18.
 */
public class GenerateScopes implements Generator {
    @Override
    public void generate(GenProject project) throws Exception {
        List<JDefinedClass> appScopes = new ArrayList<>();
        for (FullQualifiedName app : project.getDiscoveredKinds().get(DiscoveryKind.APPLICATION)) {
            appScopes.add(new ApplicationScopeGeneration().create(project, app));
        }

        if (appScopes.isEmpty()) {
            throw new Panic("you need to have exactly one application, but you currently have none");
        }

        if (appScopes.size() > 1) {
            throw new Panic("you need to have exactly one application, but you currently have " + appScopes.size());
        }

        JDefinedClass appScope = appScopes.get(0);
        List<JDefinedClass> acScopes = new ArrayList<>();
        List<FullQualifiedName> activities = new ArrayList<>(project.getDiscoveredKinds().get(DiscoveryKind.ACTIVITY));
        for (FullQualifiedName activity : activities) {
            acScopes.add(new ParentScopeGeneration(appScope).create(project, activity));
        }

        if (acScopes.isEmpty()) {
            throw new Panic("you need to have at least one activity");
        }

        JDefinedClass activitySuperSet = createCommonActivitiesScopeInterface(project.getResolver(), project.getCodeModel(), appScope, acScopes);
        for (int i = 0; i < acScopes.size(); i++) {
            JDefinedClass acScope = acScopes.get(i);
            acScope._implements(activitySuperSet.narrow(project.getCodeModel().ref(activities.get(i).toString())));
        }

        for (FullQualifiedName bindable : project.getDiscoveredKinds().get(DiscoveryKind.BIND)) {
            JDefinedClass bindScope = new ParentScopeGeneration(activitySuperSet).create(project, bindable);
        }

    }

    /**
     * Creates a super set interface for the given activities. This avoids castings
     */
    private JDefinedClass createCommonActivitiesScopeInterface(Resolver resolver, JCodeModel code, JDefinedClass appScope, List<JDefinedClass> activityScopes) throws Exception {
        Map<String, List<JMethod>> methods = new HashMap<>();
        for (JDefinedClass activityScope : activityScopes) {
            for (JMethod method : activityScope.methods()) {
                if (method.name().startsWith("get") && method.params().isEmpty()) {
                    List<JMethod> list = methods.get(method.name());
                    if (list == null) {
                        list = new ArrayList<>();
                        methods.put(method.name(), list);
                    }
                    //add all method disregarding their return type, we introduce polymorphism later
                    list.add(method);
                }
            }
        }

        JDefinedClass superSet = code._class(new FullQualifiedName(appScope.fullName()).getPackageName() + ".ActivityScope", EClassType.INTERFACE);

        superSet.javadoc().add("This interface contains all scope resources which are equal to all activities. This is the contract which is used by all other bindings.");

        JMethod getContext = null;
        //now just add those methods which are equal to all
        for (Entry<String, List<JMethod>> entry : methods.entrySet()) {
            if (entry.getValue().size() == activityScopes.size()) {
                //every one has such a named method, so try to infer common type

                List<FullQualifiedName> returnTypes = new ArrayList<>();
                for (JMethod meth : entry.getValue()) {
                    returnTypes.add(new FullQualifiedName(meth.type().fullName()));
                }
                List<FullQualifiedName> superTypes = resolver.resolveCommonSuperTypes(returnTypes);

                JMethod commonMethod = superSet.method(JMod.PUBLIC, code.ref(superTypes.get(0).toString()), entry.getKey());
                //capture the context method, to indicate if we can implement the proper interface later
                if (entry.getKey().equals("getContext")) {
                    getContext = commonMethod;
                }
            }
        }

        if (getContext != null) {
            superSet._implements(code.ref(ContextScope.class).narrow(code.directClass("T")));
            //e.g. T extends EventAppCompatActivity
            superSet.generify("T").bound(code.ref(getContext.type().fullName()));
            getContext.type(code.directClass("T"));
        } else {
            superSet._implements(Scope.class);
        }


        return superSet;
    }

    /**
     * The base scope generator creates a class for the given bean postfixed with 'Scope'.
     * For the bean itself and each @ScopeElement an accessor is created.
     */
    private static class ScopeGenerator {
        JCodeModel code;

        JDefinedClass create(GenProject project, FullQualifiedName bean) throws Exception {
            JCodeModel code = project.getCodeModel();
            this.code = code;
            Resolver resolver = project.getResolver();
            onStartGeneration(code);

            //e.g. my.domain.MyApp
            AbstractJClass beanClass = code.ref(bean.toString());

            //e.g. my.domain.MyAppScope
            JDefinedClass scope = code._class(bean.toString() + "Scope")._extends(AbsScope.class);

            //private final my.domain.MyApp myApp;
            JFieldVar fieldBean = scope.field(JMod.PRIVATE | JMod.FINAL, beanClass, Strings.startLowerCase(bean.getSimpleName()));
            scope._implements(code.ref(ContextScope.class).narrow(fieldBean.type()));

            //public MyAppScope(my.domain.MyApp myApp)
            JMethod constructor = scope.constructor(JMod.PUBLIC);
            onConstructorDefined(scope, constructor);
            JVar varBean = constructor.param(beanClass, fieldBean.name());


            //this.myApp = myApp
            constructor.body().add(JExpr._this().ref(fieldBean).assign(varBean));

            //public MyApp getMyApp(){...}
            scope.method(JMod.PUBLIC, beanClass, "get" + Strings.startUpperCase(beanClass.name())).body()._return(fieldBean);

            /*
                provide things into scope, annotated by @ScopeElement
                e.g.
                public class MyApp extends Application{
                    @ScopeElement
                    createMyCustomDatabase(){...}
                }

                leads to e.g.

                public MyCustomDatabase getMyCustomDatabase(){...}
             */
            for (Method method : project.getResolver().getMethods(bean)) {
                if (method.getAnnotation(ScopeElement.class) != null) {
                    JInvocation factoryMethod = fieldBean.invoke(method.getName());
                    AbstractJClass providedType = code.ref(method.getType().getFullQualifiedName().toString());
                    createDoubleCheckGetter(code, scope, providedType, factoryMethod);
                }

            }


            //call the PostConstruct methods
            JMethod create = scope.method(JMod.PUBLIC, void.class, "onCreate");
            create.body().add(JExpr._super().invoke("onCreate"));
            create.annotate(Override.class);
            for (Method method : resolver.getMethods(bean)) {
                if (method.getAnnotation(PostConstruct.class) != null) {
                    create.body().add(fieldBean.invoke(method.getName()));
                }
            }

            //call the PreDestroy methods
            JMethod destroy = scope.method(JMod.PUBLIC, void.class, "onDestroy");
            destroy.body().add(JExpr._super().invoke("onDestroy"));
            destroy.annotate(Override.class);
            for (Method method : resolver.getMethods(bean)) {
                if (method.getAnnotation(PreDestroy.class) != null) {
                    destroy.body().add(fieldBean.invoke(method.getName()));
                }
            }

            //getContext()
            JMethod getScopedValue = scope.method(JMod.PUBLIC, fieldBean.type(), "getContext");
            getScopedValue.annotate(Override.class);
            getScopedValue.body()._return(fieldBean);

            //<T> T resolve(Class<T> type)
            createResolveMethod(code, scope);

            return scope;
        }

        void onStartGeneration(JCodeModel code) throws Exception {

        }

        void createResolveMethod(JCodeModel code, JDefinedClass scope) {
            JDirectClass genericT = code.directClass("T");
            JMethod resolve = scope.method(JMod.PUBLIC, genericT, "resolve");
            resolve.generify("T");
            resolve.annotate(Override.class);
            JVar typeVar = resolve.param(code.ref(Class.class).narrow(genericT), "type");

            resolve.body()._if(typeVar.eqNull())._then()._return(JExpr._null());

            //this.getClass().isAssignableFrom(type)
            resolve.body()._if(JExpr._this().invoke("getClass").invoke("isAssignableFrom").arg(typeVar))._then()._return(JExpr.cast(genericT, JExpr._this()));

            //loop every member
            for (JMethod method : scope.methods()) {
                if (method.name().startsWith("get")) {
                    JVar tmpVar = resolve.body().decl(method.type(), "_" + Strings.startLowerCase(method.name().substring(3)), JExpr._this().invoke(method));
                    JInvocation assignable = tmpVar.invoke("getClass").invoke("isAssignableFrom").arg(typeVar);
                    resolve.body()._if(tmpVar.neNull().band(assignable))._then()._return(JExpr.cast(genericT, tmpVar));
                }
            }

            //delegate to parent, if any
            JVar tmpScope = resolve.body().decl(code.ref(Scope.class), "parent", JExpr._this().invoke("getParent"));
            resolve.body()._if(tmpScope.eqNull())._then()._return(JExpr._null());
            resolve.body()._return(tmpScope.invoke("resolve").arg(typeVar));
        }

        void onConstructorDefined(JDefinedClass scope, JMethod constructor) {
            //public Scope getParent(){...}
            JMethod getParent = scope.method(JMod.PUBLIC, Scope.class, "getParent");
            getParent.annotate(Override.class);
            getParent.body()._return(JExpr._null());
        }

        /*
         Creates e.g.

         private MyCustomDatabase myCustomDatabase;

         public MyCustomDatabase getMyCustomDatabase(){
            MyCustomDatabase _bean = myCustomDatabase;
            if (_bean == null) {
                synchronized (this) {
                    if (myCustomDatabase == null) {
                        myCustomDatabase = _bean = conceptApplication.createMyCustomDatabase();
                    }
                }
            }
            return _bean;
        }
         */
        void createDoubleCheckGetter(JCodeModel code, JDefinedClass where, AbstractJClass what, JInvocation factory) {
            //e.g. private volatile MyCustomDatabase;
            JFieldVar field = where.field(JMod.PRIVATE | JMod.VOLATILE, what, Strings.startLowerCase(what.name()));

            //e.g. public MyCustomDatabase getMyCustomDatabase()
            JMethod getter = where.method(JMod.PUBLIC, what, "get" + Strings.startUpperCase(what.name()));

            //if (_bean == null) {synchronized(this){}}
            JVar varBean = getter.body().decl(what, "_tmp", field);
            JSynchronizedBlock sync = getter.body()._if(varBean.eqNull())._then().synchronizedBlock(JExpr._this());
            JBlock then = sync.body()._if(field.eqNull())._then();

            //e.g.  myCustomDatabase = _bean = conceptApplication.createMyCustomDatabase();
            then.add(field.assign(JExpr.assign(varBean, factory)));

            //return _bean
            getter.body()._return(varBean);
        }
    }

    /**
     * Like the normal scope generator but includes factories for the singletons.
     */
    private static class ApplicationScopeGeneration extends ScopeGenerator {


        @Override
        JDefinedClass create(GenProject project, FullQualifiedName bean) throws Exception {
            JDefinedClass scope = super.create(project, bean);
            JCodeModel code = project.getCodeModel();
            List<FullQualifiedName> singletons = new ArrayList<>(project.getDiscoveredKinds().get(DiscoveryKind.SINGLETON));
            singletons.sort(Comparator.comparing(FullQualifiedName::getSimpleName));
            for (FullQualifiedName singleton : singletons) {
                AbstractJClass providedType = code.ref(singleton.toString());
                JMethod factoryMethod = createSingletonFactory(project.getResolver(), code, scope, providedType);
                createDoubleCheckGetter(code, scope, providedType, JExpr.invoke(factoryMethod));
            }
            return scope;
        }

        private JMethod createSingletonFactory(Resolver resolver, JCodeModel code, JDefinedClass where, AbstractJClass what) throws Exception {
            return new ObjectCreator().createFactoryMethod(resolver, code, where, what);
        }
    }

    private static class ParentScopeGeneration extends ScopeGenerator {
        final JDefinedClass parentScope;
        JDefinedClass commonParentInterface;

        public ParentScopeGeneration(JDefinedClass parentScope) {
            if (parentScope == null) {
                throw new Panic("parent scope is null");
            }
            this.parentScope = parentScope;
        }

        @Override
        void onStartGeneration(JCodeModel code) throws Exception {
            //create common parent interface, if necessary
            String commonInterfaceName = parentScope.fullName() + "Child";
            commonParentInterface = code._getClass(commonInterfaceName);
            if (commonParentInterface == null) {
                commonParentInterface = code._class(JMod.PUBLIC, commonInterfaceName, EClassType.INTERFACE);
                commonParentInterface._implements(code.ref(Scope.class));
                commonParentInterface.method(JMod.PUBLIC, parentScope, "getParent");
            }

        }

        @Override
        void onConstructorDefined(JDefinedClass scope, JMethod constructor) {
            scope._implements(commonParentInterface);
            //e.g. ActivityScope(AppScope appScope,....)
            JVar var = constructor.param(parentScope, Strings.startLowerCase(parentScope.name()));
            JFieldVar fieldBean = scope.field(JMod.PRIVATE | JMod.FINAL, parentScope, var.name());
            //this.myApp = myApp
            constructor.body().add(JExpr._this().ref(fieldBean).assign(fieldBean));

            //public MyApp getParent(){...}
            JMethod getParent = scope.method(JMod.PUBLIC, parentScope, "getParent");
            getParent.annotate(Override.class);
            getParent.body()._return(fieldBean);
        }
    }


    private static class ObjectCreator {
        JMethod createFactoryMethod(Resolver resolver, JCodeModel code, JDefinedClass where, AbstractJClass what) throws Exception {
            //e.g. private MySingleton
            JMethod creator = where.method(JMod.PRIVATE, what, "create" + Strings.startUpperCase(what.name()));
            FullQualifiedName fqn = new FullQualifiedName(what.fullName());
            Constructor shortestConstructor = null;
            for (Constructor ctr : resolver.getConstructors(fqn)) {
                if (shortestConstructor == null || ctr.getParameters().size() < ctr.getParameters().size()) {
                    shortestConstructor = ctr;
                }
            }
            JVar bean;
            if (shortestConstructor == null) {
                bean = creator.body().decl(what, "bean", JExpr._new(what));
            } else {
                JInvocation expr = JExpr._new(what);
                for (Parameter p : shortestConstructor.getParameters()) {
                    JInvocation getter = JExpr.invoke("get" + Strings.startUpperCase(p.getType().getSimpleName()));
                    expr.arg(getter);
                }
                bean = creator.body().decl(what, "bean", expr);
            }

            //fill the fields
            for (Field field : resolver.getFields(fqn)) {
                if (field.getAnnotation(Inject.class) != null) {
                    //e.g. bean.myDB = getMyCustomDatabase()
                    JAssignment beanFieldAssign = bean.ref(field.getName()).assign(JExpr.invoke("get" + Strings.startUpperCase(field.getType().getFullQualifiedName().getSimpleName())));
                    creator.body().add(beanFieldAssign);
                }
            }

            creator.body()._return(bean);
            return creator;
        }


    }
}
