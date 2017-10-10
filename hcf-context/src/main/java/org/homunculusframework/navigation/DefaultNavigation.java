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

import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.Request;
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
        request.put(Container.NAME_CALLSTACK, getCallStack(5));
        request.execute(scope).whenDone(res -> {
            Object obj = res.get();
            if (obj instanceof ModelAndView) {
                ModelAndView mav = (ModelAndView) obj;
                String uis = mav.getView();

            } else {
                LoggerFactory.getLogger(getClass()).error("invocation success, but result not useful to navigate (use ModelAndView): {} -> {}", request.getRequestMapping(), obj);
            }
        });
    }

    private StackTraceElement[] getCallStack(int offset) {

        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        return Arrays.copyOfRange(trace, offset, trace.length);
    }
}
