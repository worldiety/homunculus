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

import org.homunculusframework.concurrent.Async;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.container.Handler;
import org.homunculusframework.factory.container.MethodBinding;
import org.homunculusframework.factory.container.ObjectBinding;
import org.homunculusframework.factory.scope.ContextScope;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Reflection;
import org.homunculusframework.lang.Result;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

/**
 * This default navigation implements a simple stack based navigation approach and resolves
 * everything from a given {@link Scope}. It automatically dispatches
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
    private static final AtomicInteger requestNo = new AtomicInteger();

    private final List<Binding<?, ?>> stack;
    private boolean crashOnFail = true;
    private boolean wasGoingForward = true;
    private final Handler methodBindingHandler;
    private final Handler objectBindingHandler;

    public DefaultNavigation(Scope scope, Handler methodBindingHandler, Handler objectBindingHandler) {
        this.scope = scope;
        this.stack = new CopyOnWriteArrayList<>();
        this.methodBindingHandler = methodBindingHandler;
        this.objectBindingHandler = objectBindingHandler;
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
    public void reset(Binding<?, ?> request) {
        synchronized (stack) {
            stack.clear();
            stack.add(request);
        }
        applyInternal(request);
    }

    @Override
    public void forward(Binding<?, ?> request) {
        synchronized (stack) {
            wasGoingForward = true;
            stack.add(request);
        }
        applyInternal(request);
    }

    @Override
    public boolean backward() {
        synchronized (stack) {
            wasGoingForward = false;
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
    public void backward(Binding<?, ?> request) {
//        synchronized (stack) {
//            wasGoingForward = false;
//            pop();//just pop the current entry from the stack
//            while (!stack.isEmpty()) {
//                Binding<?,?,?> prior = pop();
//                if (prior != null && prior.equalsTarget(request)) {
//                    applyInternal(request);
//                    return;
//                }
//            }
//            stack.add(request);
//            applyInternal(request);
//        }
    }

    @Override
    public void redirect(Binding<?, ?> request) {
        applyInternal(request);
    }

    @Override
    public boolean reload() {
        Binding<?, ?> request = getTop();
        if (request != null) {
            applyInternal(request);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public Binding<?, ?> pop() {
        synchronized (stack) {
            while (!stack.isEmpty()) {
                return stack.remove(stack.size() - 1);
            }
            return null;
        }
    }

    @Override
    public void push(Binding<?, ?> request) {
        synchronized (stack) {
            stack.add(request);
        }
    }

    @Nullable
    @Override
    public Binding<?, ?> getTop() {
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
    public Binding<?, ?> getCurrent() {
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
    public Binding<?, ?> getPriorTop() {
        synchronized (stack) {
            if (stack.size() > 1) {
                return stack.get(stack.size() - 2);
            }
        }
        return null;
    }

    @Override
    public List<Binding<?, ?>> getStack() {
        return stack;
    }

    @Override
    public boolean wasGoingForward() {
        return wasGoingForward;
    }

    /**
     * Called to apply a new UIS for navigation.
     *
     * @param request the request
     */
    public void apply(Binding<?, ?> request) {
        //this double wrapping call is needed to ensure that our stack capturing always cuts at the correct depth
        applyInternal(request);
    }

    private void applyInternal(Binding<?, ?> binding) {
        UserInterfaceState uis = currentUIS;
        Binding<?, ?> currentRequest;
        if (uis != null) {
            currentRequest = uis.getRequest();
        } else {
            currentRequest = null;
        }
        postBeforeApply(currentRequest, binding);
//        binding.setStackTrace(DefaultFactory.getCallStack(0));
        if (binding instanceof MethodBinding) {
            //a method binding is async and always returns an object binding
            MethodBinding methodBinding = (MethodBinding<?>) binding;
            Task<Result<ObjectBinding>> methodTask = Async.inHandler(getScope(), methodBindingHandler, true, ctx -> {
                /*
                Currently this works always for generated code because method bindings are always tied to the common ActivityScope context, which is the same as the navigation scope (by default).
                For sure a developer may easily break that assumption, but then we cannot do much on that. This is library code and he has to write his own navigation then.
                 */
                try {
                    ObjectBinding objectBinding = (ObjectBinding) methodBinding.create(getScope());
                    return Result.create(objectBinding);
                } catch (Throwable e) {
                    return Result.auto(e);
                }
            });

            methodTask.whenDone(res -> {
                if (res.getThrowable() != null) {
                    postAfterApply(currentRequest, binding, res.getThrowable());
                } else {
                    ObjectBinding chainedBinding = res.get();
                    if (chainedBinding == null) {
                        postAfterApply(currentRequest, binding, new Panic("method binding is not allowed to return null"));
                    } else {
                        attachTask(chainedBinding, scope);
                    }
                }
            });
        } else if (binding instanceof ObjectBinding) {
            //already just an object binding
            attachTask((ObjectBinding) binding, scope);
        } else {
            //something unkown
            postAfterApply(currentRequest, binding, new Panic("binding type unknown: " + binding));
        }
    }

    /**
     * Delegates to {@link #onBeforeApply(Binding, Binding)}
     */
    protected void postBeforeApply(@Nullable Binding<?, ?> oldRequest, Binding<?, ?> newRequest) {
        onBeforeApply(oldRequest, newRequest);
    }

    /**
     * Delegates {@link #onAfterApply(Binding, Binding, Throwable)}
     */
    protected void postAfterApply(@Nullable Binding<?, ?> currentRequest, Binding<?, ?> nextRequest, @Nullable Throwable details) {
        onAfterApply(currentRequest, nextRequest, details);
    }

    /**
     * Called before a new state is applied or the old state has been changed. It is called directly from the same
     */
    protected void onBeforeApply(@Nullable Binding<?, ?> currentRequest, Binding<?, ?> nextRequest) {

    }

    /**
     * Called after the state has been applied or if application has been rejected. The throwable is not null,
     * the UIS has not been applied
     */
    protected void onAfterApply(@Nullable Binding<?, ?> oldRequest, Binding<?, ?> newRequest, @Nullable Throwable details) {

    }

    //TODO actually it is possible that requests overpass each other, this must be avoided by the callee e.g. by locking the screen
    private void attachTask(ObjectBinding binding, Scope uisScope) {
        UserInterfaceState uis = currentUIS;
        ObjectBinding<?, ?> currentRequest;
        if (uis != null) {
            currentRequest = uis.getRequest();
        } else {
            currentRequest = null;
        }

        //we don't want to interrupt the main thread, as this may cause damage to the fragile Android UI logic
        Task<Result<ContextScope>> createTask = Async.inHandler(getScope(), objectBindingHandler, false, ctx -> {
             /*
                Currently this works always for generated code because method bindings are always tied to the common ActivityScope context, which is the same as the navigation scope (by default).
                For sure a developer may easily break that assumption, but then we cannot do much on that. This is library code and he has to write his own navigation then.
                 */
            try {
                ContextScope scopeWithBoundBean = (ContextScope) binding.create(getScope());
                scopeWithBoundBean.onCreate();
                return Result.create(scopeWithBoundBean);
            } catch (Throwable e) {
                return Result.auto(e);
            }
        });

        createTask.whenDone(res -> {
            if (res.getThrowable() != null) {
                postAfterApply(currentRequest, binding, res.getThrowable());
            } else {
                tearDownOldAndApplyNew(new UserInterfaceState(binding, res.get(), res.get().getContext()));
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

            Task<Result<Void>> destroyTask = Async.inHandler(getScope(), objectBindingHandler, false, ctx -> {
                try {
                    oldUIS.getScope().onDestroy();
                    return Result.create();
                } catch (Throwable e) {
                    return Result.auto(e);
                }
            });
            destroyTask.whenDone(res -> {
                if (res.getThrowable() != null) {
                    postAfterApply(oldUIS.getRequest(), uis.getRequest(), res.getThrowable());
                } else {
                    LoggerFactory.getLogger(getClass()).info("applied UIS {}", uis);
                    currentUIS = uis;
                    postAfterApply(oldUIS.getRequest(), uis.getRequest(), null);
                }
            });

        }
    }


    /**
     * Represents a user interface state, see also {@link org.homunculusframework.stereotype.UserInterfaceState}
     */
    public final static class UserInterfaceState {
        private final ContextScope scope;
        private final Object bean;
        private final ObjectBinding<?, ?> request;

        UserInterfaceState(ObjectBinding<?, ?> request, ContextScope scope, Object bean) {
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
        public ObjectBinding<?, ?> getRequest() {
            return request;
        }
    }

    //TODO we will spawn a new thread for each method request, which is not a great idea at all
    private static class MethodCallThread extends Thread {
        private final StackTraceElement[] origin;
        private final MethodBinding<?> binding;
        private final Scope scope;

        public MethodCallThread(Scope scope, MethodBinding<?> methodBinding) {
            this.scope = scope;
            binding = methodBinding;
            origin = Thread.currentThread().getStackTrace();
            //avoid lagging the ui
            setPriority(Thread.MIN_PRIORITY);
            setName(methodBinding.toString());
        }

        @Override
        public void run() {

        }
    }
}
