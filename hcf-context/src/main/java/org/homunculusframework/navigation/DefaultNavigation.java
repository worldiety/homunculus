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
package org.homunculusframework.navigation;

import org.homunculusframework.factory.configuration.Container;
import org.homunculusframework.factory.executor.BackgroundThread;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * This default navigation implements a simple stack based navigation approach and resolves
 * everything from a given {@link Scope} and a declared {@link Container}
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class DefaultNavigation implements Navigation {
    private final Scope scope;

    public DefaultNavigation(Scope scope) {
        this.scope = scope;
    }

    @Override
    public void forward(Request request) {
        execute(request);
    }

    private void execute(Request request) {
        request.put(Container.NAME_CALLSTACK, getCallStack(4));
        Container container = scope.resolve(Container.class);
        if (container == null) {
            LoggerFactory.getLogger(getClass()).error("navigation not possible: {} missing", Container.class);
        } else {
            BackgroundThread backgroundThread = scope.resolve(BackgroundThread.class);
            Runnable job = () -> {
                try {
                    Object result = container.invoke(scope, request);
                    LoggerFactory.getLogger(getClass()).info("invocation success {}", request.getRequestMapping());
                } catch (Exception e) {
                    LoggerFactory.getLogger(getClass()).error("failed to execute Request '{}':", request.getRequestMapping(), e);
                }
            };
            if (backgroundThread == null) {
                LoggerFactory.getLogger(getClass()).warn("no background thread defined, executing Request '{}' inline", request.getRequestMapping());
                job.run();
            } else {
                backgroundThread.post(job);
            }
        }
    }

    private StackTraceElement[] getCallStack(int offset) {

        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        return Arrays.copyOfRange(trace, offset, trace.length);
    }
}
