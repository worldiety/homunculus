package org.homunculus.codegen.generator;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;

import org.homunculus.codegen.Generator;
import org.homunculus.codegen.Project;
import org.homunculus.codegen.SrcFile;
import org.homunculus.codegen.generator.GenerateAutoDiscovery.DiscoveryKind;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.async.AsyncDelegate;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.lang.Result;
import org.homunculusframework.navigation.ModelAndView;
import org.homunculusframework.navigation.Navigation;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Torben Schinke on 20.02.18.
 */

public class GenerateAsyncControllers implements Generator {


    @Override
    public void generate(Project project) throws Exception {
        for (SrcFile file : project.getDiscoveredKinds().get(DiscoveryKind.CONTROLLER)) {
            List<Method> exportedAsyncMethods = new ArrayList<>();
            List<Method> exportedModelAndViewMethods = new ArrayList<>();
            ClassOrInterfaceDeclaration ctr = file.getUnit().getClassByName(file.getPrimaryClassName()).get();
            for (MethodDeclaration meth : ctr.getMethods()) {
                String returnType = file.getFullQualifiedName(meth.getType().asString());
                if (meth.isPublic() && !ModelAndView.class.getName().equals(returnType)) {
                    exportedAsyncMethods.add(new Method(meth));
                }

                if (meth.isPublic() && ModelAndView.class.getName().equals(returnType)) {
                    exportedModelAndViewMethods.add(new Method(meth));
                }
            }

            if (exportedAsyncMethods.isEmpty() && exportedModelAndViewMethods.isEmpty()) {
                continue;
            }
            String beanName = GenerateRequestFactories.resolveName(file, ctr.getAnnotations());

            JDefinedClass jc = project.getCodeModel()._package(file.getPackageName())._class("Async" + file.getPrimaryClassName());
            jc._extends(project.getCodeModel().ref(AsyncDelegate.class).narrow(project.getCodeModel().ref(file.getFullQualifiedNamePrimaryClassName())));
            jc.headerComment().add(project.getDisclaimer(getClass()));
            jc.javadoc().add("This class provides asynchronous calls for all public methods of {@link " + file.getFullQualifiedNamePrimaryClassName() + "}. \nThis should only be used from the UI and not from within other Controllers.\nIt always expects an injected Scope to determine the lifetime of the task.\nIf you need special behavior or other methods, it is fine to extend this class.");
            for (Method m : exportedAsyncMethods) {
                String returnType = file.getFullQualifiedName(m.declaration.getType().asString());
                AbstractJClass actualReturn = project.getCodeModel().ref(returnType);
                JMethod newMeth = jc.method(JMod.PUBLIC, project.getCodeModel().ref(Task.class).narrow(project.getCodeModel().ref(Result.class).narrow(actualReturn)), m.declaration.getNameAsString());

                StringBuilder ptmp = new StringBuilder();
                for (Parameter param : m.declaration.getParameters()) {
                    newMeth.param(project.getCodeModel().ref(file.getFullQualifiedName(param.getType().asString())), param.getNameAsString());
                    ptmp.append(param.getNameAsString()).append(", ");
                }
                if (ptmp.length() > 0) {
                    ptmp.setLength(ptmp.length() - 2);
                }
                newMeth.javadoc().add("Uses {@link org.homunculusframework.factory.container.Container#NAME_BACKGROUND_HANDLER} to execute {@link " + file.getFullQualifiedNamePrimaryClassName() + "#" + m.declaration.getNameAsString() + "} asynchronously.");
                newMeth.body()._return(JExpr.direct("async(ctr -> ctr." + m.declaration.getNameAsString() + "(" + ptmp + "))"));
            }

            for (GenerateAsyncControllers.Method m : exportedModelAndViewMethods) {
                String methodName = GenerateRequestFactories.resolveName(file, m.declaration.getAnnotations());
                if (methodName == null) {
                    methodName = m.declaration.getNameAsString();
                }
                JMethod newMeth = jc.method(JMod.PUBLIC | JMod.STATIC, project.getCodeModel().ref(Request.class), m.declaration.getNameAsString());

                StringBuilder types = new StringBuilder();
                for (Parameter param : m.declaration.getParameters()) {
                    newMeth.param(project.getCodeModel().ref(file.getFullQualifiedName(param.getType().asString())), param.getNameAsString());
                    types.append(file.getFullQualifiedName(param.getType().asString())).append(",");
                }
                if (types.length() > 0) {
                    types.setLength(types.length() - 1);
                }
                newMeth.javadoc().add("Creates a request which is useful with e.g. {@link " + Navigation.class.getName() + "#forward(Request.class)} by referring to {@link " + file.getFullQualifiedNamePrimaryClassName() + "#" + m.declaration.getNameAsString() + "(" + types + ")}.");


                StringBuilder tmp = new StringBuilder();
                tmp.append("new Request(");
                if (beanName == null) {
                    tmp.append(file.getFullQualifiedNamePrimaryClassName()).append(".class").append(", \"").append(methodName).append("\"");
                } else {
                    tmp.append("\"").append(new File(beanName, methodName).toString());

                    tmp.append("\"");
                }

                tmp.append(").");

                for (Parameter param : m.declaration.getParameters()) {
                    String paramName = GenerateRequestFactories.resolveName(file, param.getAnnotations());
                    if (paramName == null) {
                        paramName = param.getNameAsString();
                    }
                    tmp.append("put(\"").append(paramName).append("\", ").append(param.getNameAsString()).append(").");
                }

                if (m.declaration.getParameters().size() > 0) {
                    tmp.setLength(tmp.length() - 1);
                }

                newMeth.body()._return(JExpr.direct(tmp.toString()));
            }

            LoggerFactory.getLogger(getClass()).info("created {} with {} methods", jc.fullName(), exportedAsyncMethods.size());

        }
    }

    static class Method {
        final MethodDeclaration declaration;

        public Method(MethodDeclaration declaration) {
            this.declaration = declaration;
        }
    }

}
