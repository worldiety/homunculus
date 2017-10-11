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
package org.homunculusframework.factory.component;

import org.homunculusframework.factory.ProcessingCompleteCallback;
import org.homunculusframework.factory.annotation.PostConstruct;
import org.homunculusframework.factory.container.Handler;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Applies the {@link PostConstruct} methods in required order and in defined executors. Failed or invalid methods are silently ignored, to avoid
 * bubbling up exceptions at undefined places in the given {@link Handler}s. Occured exceptions are forwared to {@link ProcessingCompleteCallback}
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class AMPPostConstruct implements AnnotatedMethodsProcessor {

    @Override
    public void process(Scope scope, Object instance, List<Method> methods, ProcessingCompleteCallback callback) {
        List<MyMethod> myMethods = new ArrayList<>();
        for (Method method : methods) {
            PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
            if (postConstruct != null) {
                Handler handler = scope.resolveNamedValue(postConstruct.in(), Handler.class);
                if (handler == null) {
                    LoggerFactory.getLogger(instance.getClass()).error("{} is undefined, @PostConstruct {}() ignored", postConstruct.in(), method.getName());
                    continue;
                }
                if (method.getParameterTypes().length != 0) {
                    LoggerFactory.getLogger(instance.getClass()).error("invalid method, parameters must be empty, @PostConstruct {}() ignored", postConstruct.in(), method.getName());
                    continue;
                }
                method.setAccessible(true);
                myMethods.add(new MyMethod(method, handler, postConstruct.order()));
            }
        }
        Collections.sort(myMethods, (a, b) -> {
            int x = a.order;
            int y = b.order;
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        });

        execNextMethod(scope, instance, myMethods, callback, new ArrayList<>());
    }

    private void execNextMethod(Scope scope, Object instance, List<MyMethod> methods, ProcessingCompleteCallback callback, List<Throwable> failures) {
        if (methods.isEmpty()) {
            LoggerFactory.getLogger(instance.getClass()).info("{}.@PostConstruct complete", instance.getClass().getSimpleName());
            callback.onComplete(scope, instance, failures);
        } else {
            MyMethod method = methods.remove(0);
            method.handler.post(() -> {
                LoggerFactory.getLogger(instance.getClass()).info("{}.{}()", instance.getClass().getSimpleName(), method.method.getName());
                try {
                    method.method.invoke(instance);
                } catch (Throwable t) {
                    LoggerFactory.getLogger(instance.getClass()).error("{}.{}() -> {}", instance.getClass().getSimpleName(), method.method.getName(), t);
                    failures.add(t);
                }
                execNextMethod(scope, instance, methods, callback, failures);
            });
        }
    }

    private final static class MyMethod {
        private final Method method;
        private final Handler handler;
        private final int order;

        MyMethod(Method method, Handler handler, int order) {
            this.method = method;
            this.handler = handler;
            this.order = order;
        }

    }
}
