package org.homunculus.codegen.generator;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
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
import org.homunculusframework.lang.Result;
import org.homunculusframework.navigation.ModelAndView;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Torben Schinke on 20.02.18.
 */

public class GenerateAsyncControllers implements Generator {


    @Override
    public void generate(Project project) throws Exception {
        for (SrcFile file : project.getDiscoveredKinds().get(DiscoveryKind.CONTROLLER)) {
            List<Method> exportedAsyncMethods = new ArrayList<>();
            ClassOrInterfaceDeclaration ctr = file.getUnit().getClassByName(file.getPrimaryClassName()).get();
            for (MethodDeclaration meth : ctr.getMethods()) {
                String returnType = file.getFullQualifiedName(meth.getType().asString());
                if (meth.isPublic() && !ModelAndView.class.getName().equals(returnType)) {
                    exportedAsyncMethods.add(new Method(meth));
                }
            }

            if (!exportedAsyncMethods.isEmpty()) {
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
//                    body.invoke(JExpr.ref(varCfg.name()), "register").arg(JExpr.direct(file.getFullQualifiedNamePrimaryClassName() + ".class"));
                    //  return async( ctr -> ctr.getPoJoCart(cartId));
                    newMeth.javadoc().add("Uses {@link org.homunculusframework.factory.container.Container#NAME_BACKGROUND_HANDLER} to execute {@link " + file.getFullQualifiedNamePrimaryClassName() + "#" + m.declaration.getNameAsString() + "} asynchronously.");
                    newMeth.body()._return(JExpr.direct("async(ctr -> ctr." + m.declaration.getNameAsString() + "(" + ptmp + "))"));
                }

            }

        }
    }

    static class Method {
        final MethodDeclaration declaration;

        public Method(MethodDeclaration declaration) {
            this.declaration = declaration;
        }
    }

    //                    for (AnnotationExpr annotation : meth.getAnnotations()) {
//                        String fqn = file.getFullQualifiedName(annotation.getNameAsString());
//                        LoggerFactory.getLogger(getClass()).info("{} -> {} @{}", meth.getName(), returnType, fqn);
//                        if (Named.NAMED.match(fqn)) {
//
//                        }else{
//                            exportedAsyncMethods.add(new Method(meth));
//                        }
//                    }
//    enum Named {
//        NAMED("javax.inject.Named");
//        private String[] named;
//
//        Named(String... args) {
//            named = args;
//        }
//
//        boolean match(String name) {
//            for (String str : named) {
//                if (str.equals(name)) {
//                    return true;
//                }
//            }
//            return false;
//        }
//
//        String getName(AnnotationExpr expr) {
//            return "";
//        }
//    }
}
