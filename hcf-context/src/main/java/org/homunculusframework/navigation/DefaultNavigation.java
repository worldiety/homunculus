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
    private UserInterfaceState currentUIS;
    private static final AtomicInteger requestNo = new AtomicInteger();

    private final List<Request> stack;

    public DefaultNavigation(Scope scope) {
        this.scope = scope;
        this.stack = new CopyOnWriteArrayList<>();
    }

    /**
     * Returns the current user interface state, which may be null if not defined.
     * Note that "current" is not well defined in transition state, because when
     * applying a new one, both states exists at the same time, which is prone to
     * logical races.
     *
     * @return the current state
     */
    @Nullable
    public UserInterfaceState getUserInterfaceState() {
        return currentUIS;
    }

    @Override
    public void forward(Request request) {
        synchronized (stack) {
            stack.add(request);
        }
        applyInternal(request);
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
            applyInternal(stack.get(stack.size() - 1));
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
                    applyInternal(prior);
                    return;
                }
            }
            stack.add(request);
            applyInternal(request);
        }
    }

    @Override
    public void redirect(Request request) {
        applyInternal(request);
    }

    @Override
    public boolean reload() {
        Request request = getTop();
        if (request != null) {
            applyInternal(request);
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
            UserInterfaceState uis = currentUIS;
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
        //this double wrapping call is needed to ensure that our stack capturing always cuts at the correct depth
        applyInternal(request);
    }

    private void applyInternal(Request request) {
        request.put(Container.NAME_CALLSTACK, DefaultFactory.getCallStack(0));
        request.execute(scope).whenDone(res -> {
            final Container container = scope.resolve(Container.NAME_CONTAINER, Container.class);
            if (container == null) {
                LoggerFactory.getLogger(getClass()).error("no container found in scope, request discarded");
                return;
            }

            Object obj = res.get();
            if (obj instanceof ModelAndView) {
                ModelAndView mav = (ModelAndView) obj;
                Scope uisScope = createChild(scope, mav);
                container.prepareScope(uisScope);
                Task<Component<?>> task = container.createWidget(uisScope, mav.getView());
                attachComponentTask(request, uisScope, task);
            } else if (obj instanceof Component) {
                Scope uisScope = ((Component) obj).getScope();
                SettableTask<Component<?>> task = SettableTask.create(uisScope, "execute tmp");
                task.set((Component) obj);
                attachComponentTask(request, uisScope, task);
            } else {
                if (obj == null) {
                    LoggerFactory.getLogger(getClass()).error("invocation returned 'null' component, not useful for navigation: {} -> {}", request.getMapping(), obj);
                } else {
                    LoggerFactory.getLogger(getClass()).error("expected a 'component' or 'ModelAndView' but received '{}'", obj);
                }
            }
        });
    }

    private void attachComponentTask(Request request, Scope uisScope, Task<Component<?>> task) {
        task.whenDone(component -> {
            Object newUIS = component.get();
            if (newUIS != null && component.getFailures().isEmpty()) {
                uisScope.put("$" + System.identityHashCode(newUIS), newUIS);
                tearDownOldAndApplyNew(new UserInterfaceState(request, uisScope, newUIS));
            } else {
                uisScope.destroy();
                LoggerFactory.getLogger(getClass()).error("denied applying UIS: {}", newUIS);
                for (Throwable t : component.getFailures()) {
                    LoggerFactory.getLogger(getClass()).error("created failed", t);
                }
            }
        });
    }

    private void tearDownOldAndApplyNew(UserInterfaceState uis) {
        UserInterfaceState oldUIS = currentUIS;
        if (oldUIS == null) {
            currentUIS = uis;
            LoggerFactory.getLogger(getClass()).info("applied UIS {}", uis);
        } else {
            Container container = scope.resolve(Container.NAME_CONTAINER, Container.class);
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
            scope.put(entry.getKey(), entry.getValue());
            return true;
        });
        return scope;
    }

    public static Scope createChild(Scope parent, Request params) {
        Scope scope = new Scope("request:" + params.getMapping() + "@" + requestNo.incrementAndGet(), parent);
        params.forEachEntry(entry -> {
            scope.put(entry.getKey(), entry.getValue());
            return true;
        });
        return scope;
    }


    /**
     * Represents a user interface state, see also {@link org.homunculusframework.stereotype.UserInterfaceState}
     */
    public final static class UserInterfaceState {
        private final Scope scope;
        private final Object widget;
        private final Request request;

        UserInterfaceState(Request request, Scope scope, Object widget) {
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

        /**
         * Returns the scope in which this UIS exists
         *
         * @return the scope
         */
        public Scope getScope() {
            return scope;
        }

        /**
         * Returns the instantiated widget class which may be anything, even a simple pojo.
         * Usually a UIS applies itself to the screen or window with a  {@link javax.annotation.PostConstruct}
         * and is annotated itself with {@link org.homunculusframework.factory.flavor.hcf.Widget}.
         *
         * @return the widget
         */
        public Object getWidget() {
            return widget;
        }

        /**
         * Returns the request which caused this UIS.
         *
         * @return the request
         */
        public Request getRequest() {
            return request;
        }
    }
}
