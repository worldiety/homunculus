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
import com.helger.jcodemodel.JDirectClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JNarrowedClass;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.generator.PreprocessDiscoverBeans.DiscoveryKind;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Strings;
import org.homunculusframework.concurrent.Async;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.container.ObjectContainer;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates the factory for the singletons / controllers.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class GenerateControllerFactory implements Generator {
    @Override
    public void generate(GenProject project) throws Exception {
        JDefinedClass container = project.getCodeModel()._class(project.getManifestPackage() + ".Controllers");
        container.headerComment().add(project.getDisclaimer(getClass()));
        container.javadoc().add("Provides a central management point for all available singleton controllers.");
        container._implements(ObjectContainer.class);


        List<FullQualifiedName> controllers = new ArrayList<>();
        for (FullQualifiedName bean : project.getDiscoveredKinds().get(DiscoveryKind.SINGLETON)) {

            controllers.add(bean);
            JDefinedClass ctrBinding = project.getCodeModel()._getClass(bean.getPackageName() + ".Bind" + bean.getSimpleName());
            if (ctrBinding == null) {
                throw new InternalError("binding for " + bean + " not found");
            }
        }

        sortByDependencyGraph(controllers);


        //add start method and special members
        AbstractJClass taskListResultObject = project.getCodeModel().ref(Task.class).narrow(project.getCodeModel().ref(List.class).narrow(project.getCodeModel().ref(Result.class).narrow(Object.class)));
        JMethod start = container.method(JMod.PUBLIC, taskListResultObject, "start");
        start.annotate(Override.class);
        JVar varScope = start.param(Scope.class, "scope");
        JVar varRes = container.field(JMod.PRIVATE, project.getCodeModel().ref(SettableTask.class).narrow(project.getCodeModel().ref(List.class).narrow(project.getCodeModel().ref(Result.class).narrow(Object.class))), "result");
        JVar varResList = container.field(JMod.PRIVATE, project.getCodeModel().ref(List.class).narrow(project.getCodeModel().ref(Result.class).narrow(Object.class)), "resultList");

        //blocking method for getters
        JMethod awaitStart = container.method(JMod.PRIVATE, void.class, "awaitStart");
        awaitStart.body().directStatement("if (result == null) throw new RuntimeException(\"not yet started\");");
        awaitStart.body().add(project.getCodeModel().ref(Async.class).staticInvoke("await").arg(varRes));
        start.body().assign(varRes, project.getCodeModel().ref(SettableTask.class).staticInvoke("create").arg(varScope).arg("startControllers"));
        start.body().assign(varResList, JExpr._new(project.getCodeModel().ref(ArrayList.class).narrowEmpty()));

        //upcounting method for results
        JDirectClass genericType = project.getCodeModel().directClass("T");
        JMethod inc = container.method(JMod.PRIVATE | JMod.SYNCHRONIZED, genericType, "inc");
        inc.generify("T");
        JNarrowedClass parameterType = project.getCodeModel().ref(Result.class).narrow(genericType);
        JVar incR = inc.param(parameterType, "res");
        inc.body().directStatement("resultList.add((Result<Object>)res);");
        inc.body().directStatement("if (resultList.size() == " + controllers.size() + ") result.set(resultList);");
        inc.body()._return(incR.invoke("get"));


        //content of start and fields and implement start method
        for (FullQualifiedName bean : controllers) {
            JDefinedClass ctrBinding = project.getCodeModel()._getClass(bean.getPackageName() + ".Bind" + bean.getSimpleName());
            AbstractJClass absCtr = project.getCodeModel().ref(bean.toString());

            //add the member
            JFieldVar varCtr = container.field(JMod.PRIVATE, absCtr, Strings.startLowerCase(bean.getSimpleName()));

            //add the async member setter
            JInvocation exec = JExpr._new(ctrBinding).invoke("execute").arg(varScope);
            start.body().add(exec.invoke("whenDone").arg(JExpr.direct("res -> { " + varCtr.name() + " = inc(res);}")));

            //add the getter
            JMethod getter = container.method(JMod.PUBLIC, absCtr, "get" + Strings.startUpperCase(varCtr.name()));
            getter.body().invoke(awaitStart);
            getter.body()._return(varCtr);

        }
        start.body()._return(varRes);
    }

    private void sortByDependencyGraph(List<FullQualifiedName> files) {
        //TODO also detect cycles and throw
        Collections.sort(files, (a, b) -> a.getSimpleName().compareTo(b.getSimpleName()));
    }
}
