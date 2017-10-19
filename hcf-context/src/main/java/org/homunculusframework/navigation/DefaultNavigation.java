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
import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.factory.container.Component;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.lang.Reflection;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
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
        this.stack = new CopyOnWriteArrayList<>();
    }

    @Override
    public void forward(Request request) {
        synchronized (stack) {
            stack.add(request);
        }
        apply(request);
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
            apply(stack.get(stack.size() - 1));
            return true;
        }
    }

    @Override
    public void backward(Request request) {
        synchronized (stack) {
            pop();//just pop the current entry from the stack
            while (!stack.isEmpty()) {
                Request prior = pop();
                if (prior.getMapping().equals(request.getMapping())) {
                    prior.putAll(request);
                    apply(prior);
                    return;
                }
            }
            stack.add(request);
            apply(request);
        }
    }

    @Override
    public void redirect(Request request) {
        apply(request);
    }

    @Override
    public boolean reload() {
        Request request = getTop();
        if (request != null) {
            apply(request);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public Request pop() {
        synchronized (stack) {
            while (!stack.isEmpty()) {
                return stack.remove(stack.size() - 1);
            }
            return null;
        }
    }

    @Override
    public void push(Request request) {
        synchronized (stack) {
            stack.add(request);
        }
    }

    @Nullable
    @Override
    public Request getTop() {
        synchronized (stack) {
            if (stack.isEmpty()) {
                return null;
            } else {
                return stack.get(stack.size() - 1);
            }
        }
    }

    @Nullable
    @Override
    public Request getCurrent() {
        synchronized (stack) {
            UIS uis = currentUIS;
            if (uis != null) {
                return uis.request;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Request getPriorTop() {
        synchronized (stack) {
            if (stack.size() > 1) {
                return stack.get(stack.size() - 2);
            }
        }
        return null;
    }

    @Override
    public List<Request> getStack() {
        return stack;
    }

    /**
     * Called to apply a new UIS for navigation.
     *
     * @param request the request
     */
    public void apply(Request request) {
        request.put(Container.NAME_CALLSTACK, DefaultFactory.getCallStack(5));
        request.execute(scope).whenDone(res -> {
            final Container container = scope.resolveNamedValue(Container.NAME_CONTAINER, Container.class);
            if (container == null) {
                LoggerFactory.getLogger(getClass()).error("no container found in scope, request discarded");
                return;
            }

            Object obj = res.get();
            if (obj instanceof ModelAndView) {
                ModelAndView mav = (ModelAndView) obj;
                Scope uisScope = createChild(scope, mav);
                container.createProxies(uisScope);
                Task<Component<?>> task = container.createWidget(uisScope, mav.getView());
                attachComponentTask(request, uisScope, task);
            } else if (obj instanceof Component) {
                Scope uisScope = ((Component) obj).getScope();
                SettableTask<Component<?>> task = SettableTask.create(uisScope, "execute tmp");
                task.set((Component) obj);
                attachComponentTask(request, uisScope, task);
            } else {
                LoggerFactory.getLogger(getClass()).error("invocation success, but result not useful to navigate (use ModelAndView): {} -> {}", request.getMapping(), obj);
            }
        });
    }

    private void attachComponentTask(Request request, Scope uisScope, Task<Component<?>> task) {
        task.whenDone(component -> {
            Object newUIS = component.get();
            if (newUIS != null && component.getFailures().isEmpty()) {
                uisScope.putNamedValue("$" + System.identityHashCode(newUIS), newUIS);
                tearDownOldAndApplyNew(new UIS(request, uisScope, newUIS));
            } else {
                uisScope.destroy();
                LoggerFactory.getLogger(getClass()).error("denied applying UIS: {}", newUIS);
                for (Throwable t : component.getFailures()) {
                    LoggerFactory.getLogger(getClass()).error("created failed", t);
                }
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


    public static Scope createChild(Scope parent, ModelAndView modelAndView) {
        Scope scope = new Scope("inflate:" + modelAndView.getView() + "@" + requestNo.incrementAndGet(), parent);
        modelAndView.forEach(entry -> {
            scope.putNamedValue(entry.getKey(), entry.getValue());
            return true;
        });
        return scope;
    }

    public static Scope createChild(Scope parent, Request params) {
        Scope scope = new Scope("request:" + params.getMapping() + "@" + requestNo.incrementAndGet(), parent);
        params.forEach(entry -> {
            scope.putNamedValue(entry.getKey(), entry.getValue());
            return true;
        });
        return scope;
    }


    private static class UIS {
        private final Scope scope;
        private final Object widget;
        private final Request request;

        UIS(Request request, Scope scope, Object widget) {
            this.request = request;
            this.scope = scope;
            this.widget = widget;
        }

        @Override
        public String toString() {
            if (widget != null) {
                return Reflection.getName(widget.getClass());
            } else {
                return "uis(null)";
            }
        }
    }
}
