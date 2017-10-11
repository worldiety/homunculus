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

import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.container.Component;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.lang.Classname;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This default navigation implements a simple stack based navigation approach and resolves
 * everything from a given {@link Scope} and a declared {@link Container}
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class DefaultNavigation implements Navigation {
    private final Scope scope;
    @Nullable
    private UIS currentUIS;
    private static final AtomicInteger requestNo = new AtomicInteger();

    private final List<Request> stack;

    public DefaultNavigation(Scope scope) {
        this.scope = scope;
        this.stack = new ArrayList<>();
    }

    @Override
    public void forward(Request request) {
        synchronized (stack) {
            stack.add(request);
        }
        execute(request);
    }

    @Override
    public boolean backward() {
        synchronized (stack) {
            if (stack.isEmpty()) {
                return false;
            }
            //remove the current active entry
            stack.remove(stack.size() - 1);
            if (stack.isEmpty()) {
                return false;
            }
            //just execute it once again, so that it get's applied
            execute(stack.get(stack.size() - 1));
            return true;
        }
    }

    private void execute(Request request) {
        request.put(Container.NAME_CALLSTACK, getCallStack(5));
        request.execute(scope).whenDone(res -> {
            Object obj = res.get();
            if (obj instanceof ModelAndView) {
                ModelAndView mav = (ModelAndView) obj;
                Container container = scope.resolveNamedValue(Container.NAME_CONTAINER, Container.class);
                if (container == null) {
                    LoggerFactory.getLogger(getClass()).error("no container found in scope, request discarded");
                } else {
                    Scope uisScope = createChild(scope, mav);
                    Task<Component<?>> task = container.createWidget(uisScope, mav.getView());
                    task.whenDone(component -> {
                        Object newUIS = component.get();
                        if (newUIS != null && component.getFailures().isEmpty()) {
                            uisScope.putNamedValue("$" + mav.getView(), newUIS);
                            tearDownOldAndApplyNew(new UIS(uisScope, newUIS));
                        } else {
                            uisScope.destroy();
                            LoggerFactory.getLogger(getClass()).error("denied applying UIS: {}", newUIS);
                            for (Throwable t : component.getFailures()) {
                                LoggerFactory.getLogger(getClass()).error("created failed", t);
                            }
                        }
                    });
                }
            } else {
                LoggerFactory.getLogger(getClass()).error("invocation success, but result not useful to navigate (use ModelAndView): {} -> {}", request.getRequestMapping(), obj);
            }
        });
    }

    private void tearDownOldAndApplyNew(UIS uis) {
        UIS oldUIS = currentUIS;
        if (oldUIS == null) {
            currentUIS = uis;
            LoggerFactory.getLogger(getClass()).info("applied UIS {}", uis);
        } else {
            Container container = scope.resolveNamedValue(Container.NAME_CONTAINER, Container.class);
            if (container == null) {
                LoggerFactory.getLogger(getClass()).error("no container found in scope, tearDownOldAndApplyNew discarded");
            } else {
                container.destroyComponent(oldUIS.scope, oldUIS.widget, true).whenDone(component -> {
                    if (!component.getFailures().isEmpty()) {
                        LoggerFactory.getLogger(getClass()).error("problems destroying UIS: {}", component.get());
                        for (Throwable t : component.getFailures()) {
                            LoggerFactory.getLogger(getClass()).error("created failed", t);
                        }
                    }
                    LoggerFactory.getLogger(getClass()).info("applied UIS {}", uis);
                    currentUIS = uis;
                });
            }
        }
    }

    private StackTraceElement[] getCallStack(int offset) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        return Arrays.copyOfRange(trace, offset, trace.length);
    }


    public static Scope createChild(Scope parent, ModelAndView modelAndView) {
        Scope scope = new Scope("inflate:" + modelAndView.getView() + "@" + requestNo.incrementAndGet(), parent);
        modelAndView.forEach(entry -> {
            scope.putNamedValue(entry.getKey(), entry.getValue());
            return true;
        });
        return scope;
    }


    private static class UIS {
        private final Scope scope;
        private final Object widget;


        UIS(Scope scope, Object widget) {
            this.scope = scope;
            this.widget = widget;
        }

        @Override
        public String toString() {
            if (widget != null) {
                return Classname.getName(widget.getClass());
            } else {
                return "uis(null)";
            }
        }
    }
}
