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
import org.homunculusframework.lang.Panic;
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
 * everything from a given {@link Scope} and a declared {@link Container}. It automatically dispatches
 * calls to {@link #backward()} automatically to {@link UserInterfaceState#getBean()} first (if that
 * implements {@link BackActionConsumer}.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class DefaultNavigation implements Navigation {
    private final Scope scope;
    @Nullable
    private UserInterfaceState currentUIS;
    @Deprecated
    private static final AtomicInteger requestNo = new AtomicInteger();

    private final List<Request> stack;
    private boolean crashOnFail = true;

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

    /**
     * The scope in which this navigation lives.
     *
     * @return the scope
     */
    protected Scope getScope() {
        return scope;
    }

    @Override
    public void reset(Request request) {
        synchronized (stack) {
            stack.clear();
            stack.add(request);
        }
        applyInternal(request);
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
            UserInterfaceState uis = currentUIS;
            if (uis != null && uis.getBean() instanceof BackActionConsumer) {
                if (((BackActionConsumer) uis.getBean()).backward()) {
                    return true;
                }
            }
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
        UserInterfaceState uis = currentUIS;
        Request currentRequest;
        if (uis != null) {
            currentRequest = uis.getRequest();
        } else {
            currentRequest = null;
        }
        postBeforeApply(currentRequest, request);
        request.put(Container.NAME_CALLSTACK, DefaultFactory.getCallStack(0));
        request.execute(scope).whenDone(res -> {
            try {
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
                    Task<Component<?>> task;
                    switch (mav.getView().getMappingType()) {
                        case CLASS:
                            task = container.createBean(uisScope, mav.getView().getType());
                            break;
                        case NAME:
                            task = container.createBean(uisScope, mav.getView().getName());
                            break;
                        default:
                            throw new Panic();
                    }

                    attachComponentTask(request, uisScope, task);
                } else if (obj instanceof Component) {
                    Scope uisScope = ((Component) obj).getScope();
                    SettableTask<Component<?>> task = SettableTask.create(uisScope, "execute tmp");
                    task.set((Component) obj);
                    attachComponentTask(request, uisScope, task);
                } else {
                    if (obj == null) {
                        LoggerFactory.getLogger(getClass()).error("invocation returned 'null' component, not useful for navigation: {} -> {}", request.getMapping(), obj);
                        //remove the request silently from the stack, otherwise the behavior is weired, because one can go back "nothing"
                        if (request == getTop()) {
                            pop();
                        }
                        postAfterApply(currentRequest, request, res.getThrowable());
                    } else {
                        LoggerFactory.getLogger(getClass()).error("expected a 'component' or 'ModelAndView' but received '{}'", obj);
                        postAfterApply(currentRequest, request, new RuntimeException("expected a 'component' or 'ModelAndView' but received " + obj));
                    }
                }
            } catch (RuntimeException | Error e) {
                postAfterApply(currentRequest, request, e);
                throw e;
            }
        });
    }

    /**
     * Delegates to {@link #onBeforeApply(Request, Request)}
     */
    protected void postBeforeApply(@Nullable Request oldRequest, Request newRequest) {
        onBeforeApply(oldRequest, newRequest);
    }

    /**
     * Delegates {@link #onAfterApply(Request, Request, Throwable)}
     */
    protected void postAfterApply(@Nullable Request currentRequest, Request nextRequest, @Nullable Throwable details) {
        onAfterApply(currentRequest, nextRequest, details);
    }

    /**
     * Called before a new state is applied or the old state has been changed. It is called directly from the same
     */
    protected void onBeforeApply(@Nullable Request currentRequest, Request nextRequest) {

    }

    /**
     * Called after the state has been applied or if application has been rejected. The throwable is not null,
     * the UIS has not been applied
     */
    protected void onAfterApply(@Nullable Request oldRequest, Request newRequest, @Nullable Throwable details) {

    }

    private void attachComponentTask(Request request, Scope uisScope, Task<Component<?>> task) {
        UserInterfaceState uis = currentUIS;
        Request currentRequest;
        if (uis != null) {
            currentRequest = uis.getRequest();
        } else {
            currentRequest = null;
        }

        task.whenDone(component -> {
            Object newUIS = component.get();
            if (newUIS != null && component.getFailures().isEmpty()) {
                uisScope.put("$" + System.identityHashCode(newUIS), newUIS);
                tearDownOldAndApplyNew(new UserInterfaceState(request, uisScope, newUIS));
            } else {
                uisScope.destroy();
                LoggerFactory.getLogger(getClass()).error("denied applying UIS: {}", newUIS);

                if (!component.getFailures().isEmpty()) {
                    postAfterApply(currentRequest, request, component.getFailures().get(0));
                    if (crashOnFail) {
                        throw new RuntimeException("failed to create bean", component.getFailures().get(0));
                    } else {
                        for (Throwable t : component.getFailures()) {
                            LoggerFactory.getLogger(getClass()).error("created failed", t);
                        }
                    }
                } else {
                    postAfterApply(currentRequest, request, new RuntimeException("denied applying UIS: " + newUIS));
                }
            }
        });
    }

    private void tearDownOldAndApplyNew(UserInterfaceState uis) {
        UserInterfaceState oldUIS = currentUIS;
        if (oldUIS == null) {
            currentUIS = uis;
            postAfterApply(null, uis.getRequest(), null);
            LoggerFactory.getLogger(getClass()).info("applied UIS {}", uis);
        } else {
            Container container = scope.resolve(Container.NAME_CONTAINER, Container.class);
            if (container == null) {
                LoggerFactory.getLogger(getClass()).error("no container found in scope, tearDownOldAndApplyNew discarded");
                postAfterApply(oldUIS.getRequest(), uis.getRequest(), new RuntimeException("no container found in scope, tearDownOldAndApplyNew discarded"));
            } else {
                container.destroyComponent(oldUIS.scope, oldUIS.bean, true).whenDone(component -> {
                    if (!component.getFailures().isEmpty()) {
                        LoggerFactory.getLogger(getClass()).error("problems destroying UIS: {}", component.get());

                        postAfterApply(oldUIS.getRequest(), uis.getRequest(), component.getFailures().get(0));
                        if (crashOnFail) {
                            throw new RuntimeException("failed to destroy bean", component.getFailures().get(0));
                        } else {
                            for (Throwable t : component.getFailures()) {
                                LoggerFactory.getLogger(getClass()).error("destroy failed", t);
                            }
                        }
                    }
                    LoggerFactory.getLogger(getClass()).info("applied UIS {}", uis);
                    currentUIS = uis;
                    if (component.getFailures().isEmpty()) {
                        postAfterApply(oldUIS.getRequest(), uis.getRequest(), null);
                    }
                });
            }
        }
    }


    @Deprecated
    public static Scope createChild(Scope parent, ModelAndView modelAndView) {
        Scope scope = new Scope("inflate:" + modelAndView.getView() + "@" + requestNo.incrementAndGet(), parent);
        modelAndView.forEach(entry -> {
            scope.put(entry.getKey(), entry.getValue());
            return true;
        });
        return scope;
    }

    @Deprecated
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
        private final Object bean;
        private final Request request;

        UserInterfaceState(Request request, Scope scope, Object bean) {
            this.request = request;
            this.scope = scope;
            this.bean = bean;
        }

        @Override
        public String toString() {
            if (bean != null) {
                return Reflection.getName(bean.getClass());
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
         * Returns the instantiated bean class which may be anything, even a simple pojo.
         * Usually a UIS applies itself to the screen or window with a  {@link javax.annotation.PostConstruct}
         * and is annotated itself with {@link javax.inject.Named}.
         *
         * @return the bean
         */
        public Object getBean() {
            return bean;
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
