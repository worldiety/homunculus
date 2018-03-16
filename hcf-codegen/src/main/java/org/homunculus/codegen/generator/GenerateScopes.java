package org.homunculus.codegen.generator;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JAssignment;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

/**
 * Creates individual scopes for Applications, Activities and Bind-ables (== User interface states). These guys form a
 * hierarchy with some hardcoded behavior, e.g.
 * <ul>
 * <li>Application scopes contain the singletons</li>
 * <li>Activity scopes refer to the application (assuming only one) => TODO this means you cannot use the same Activity scope with different applications</li>
 * <li>Bindable scopes refer to the activity scope (assuming only one) as their parent => TODO this means you cannot use the same Bindable with different activities</li>
 * <li>Each base class can provide factory methods for their scope by using @ScopeElement on their public or package private members</li>
 * </ul>
 * <p>
 * Created by Torben Schinke on 16.03.18.
 */
public class GenerateScopes implements Generator {
    @Override
    public void generate(GenProject project) throws Exception {
        JDefinedClass appScope = null;
        for (FullQualifiedName app : project.getDiscoveredKinds().get(DiscoveryKind.APPLICATION)) {
            appScope = new ApplicationScopeGeneration().create(project, app);
        }

        JDefinedClass acScope = null;
        for (FullQualifiedName activity : project.getDiscoveredKinds().get(DiscoveryKind.ACTIVITY)) {
            acScope = new ParentScopeGeneration(appScope).create(project, activity);
        }

        for (FullQualifiedName bindable : project.getDiscoveredKinds().get(DiscoveryKind.BIND)) {
            new ParentScopeGeneration(acScope).create(project, bindable);
        }
    }

    /**
     * The base scope generator creates a class for the given bean postfixed with 'Scope'.
     * For the bean itself and each @ScopeElement an accessor is created.
     */
    private static class ScopeGenerator {

        JDefinedClass create(GenProject project, FullQualifiedName bean) throws Exception {
            JCodeModel code = project.getCodeModel();
            //e.g. my.domain.MyApp
            AbstractJClass beanClass = code.ref(bean.toString());

            //e.g. my.domain.MyAppScope
            JDefinedClass scope = code._class(bean.toString() + "Scope");

            //private final my.domain.MyApp myApp;
            JFieldVar fieldBean = scope.field(JMod.PRIVATE | JMod.FINAL, beanClass, Strings.startLowerCase(bean.getSimpleName()));

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

            return scope;
        }

        void onConstructorDefined(JDefinedClass scope, JMethod constructor) {

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

        public ParentScopeGeneration(JDefinedClass applicationScope) {
            this.parentScope = applicationScope;
        }

        @Override
        void onConstructorDefined(JDefinedClass scope, JMethod constructor) {
            //e.g. ActivityScope(AppScope appScope,....)
            JVar var = constructor.param(parentScope, Strings.startLowerCase(parentScope.name()));
            JFieldVar fieldBean = scope.field(JMod.PRIVATE | JMod.FINAL, parentScope, var.name());
            //this.myApp = myApp
            constructor.body().add(JExpr._this().ref(fieldBean).assign(fieldBean));

            //public MyApp getMyApp(){...}
            scope.method(JMod.PUBLIC, parentScope, "getParent").body()._return(fieldBean);
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
