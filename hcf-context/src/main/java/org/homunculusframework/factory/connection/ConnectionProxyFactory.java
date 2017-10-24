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
package org.homunculusframework.factory.connection;

import org.homunculusframework.concurrent.Task;
import org.homunculusframework.concurrent.ThreadInterruptible;
import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.Handler;
import org.homunculusframework.factory.container.RequestContext;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Ref;
import org.homunculusframework.lang.Reflection;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implements a proxy for a {@link Connection} declaration. See {@link Connection}
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class ConnectionProxyFactory<T> {
    private final T controllerInstance;
    private final Class<? extends Connection<T>> asyncContract;
    private final static AtomicInteger mCounter = new AtomicInteger();
    private final LinkedBlockingQueue<MyInvocationHandler> proxies;

    public ConnectionProxyFactory(T controllerInstance, Class<? extends Connection<T>> asyncContract, int hotInstances) {
        this.proxies = new LinkedBlockingQueue<>();
        this.controllerInstance = controllerInstance;
        this.asyncContract = asyncContract;
        for (int i = 0; i < hotInstances; i++) {
            proxies.add(new MyInvocationHandler(controllerInstance, asyncContract));
        }
    }

    public Class<T> getControllerType() {
        return (Class<T>) controllerInstance.getClass();
    }

    /**
     * Returns the actual connection, which is implemented using the proxy mechanism. Creates new instances when required
     */
    public Connection<T> borrowConnection(Scope scope) {
        MyInvocationHandler handler = proxies.peek();
        if (handler == null) {
            handler = new MyInvocationHandler(controllerInstance, asyncContract);
        }
        handler.currentScope = scope;
        return (Connection<T>) handler.actualProxy;
    }

    /**
     * Returns the associated handler back into the pool and resets the scope
     */
    public void returnConnection(Connection<T> connection) {
        MyInvocationHandler handler = (MyInvocationHandler) Proxy.getInvocationHandler(connection);
        handler.currentScope = null;
        proxies.add(handler);
    }

    private static class ConnectionMethod {
        private final Object instance;
        private boolean notImplemented;
        private final Method instanceTarget;
        //used to detect and flag crossing concurrent invocations -> only the last call is not flagged as outdated
        private final AtomicInteger callGeneration;
        private final boolean interruptible;

        public ConnectionMethod(Object instance, Method instanceTarget, boolean notImplemented) {
            this.instance = instance;
            this.notImplemented = notImplemented;
            this.instanceTarget = instanceTarget;
            this.callGeneration = new AtomicInteger();
            this.interruptible = instanceTarget.getAnnotation(ThreadInterruptible.class) != null;
        }

        Task<Result<?>> invoke(Scope lifeTime, Method ifaceMethod, Handler handler, Object[] args) {
            if (notImplemented) {
                SettableTask<Result<?>> task = SettableTask.create(lifeTime, ifaceMethod.getName() + "@" + mCounter.incrementAndGet());
                String sig = Reflection.getName(instance.getClass()) + "." + ifaceMethod.getName();
                RuntimeException e = new RuntimeException("connection signature invalid: " + sig);
                //TODO offset varies between android platform, e.g. S3 needs 6 but PixelXL needs 7
                e.setStackTrace(DefaultFactory.getCallStack(4)); //create a short stack trace, directly pointing to the callee
                task.set(Result.
                        create().
                        putTag("signature.missing", sig).
                        setThrowable(e)
                );
                return task;
            }
            final int myGeneration = callGeneration.incrementAndGet();
            //capture the synchronous trace
            StackTraceElement[] trace = DefaultFactory.getCallStack(4);
            SettableTask<Result<?>> task = SettableTask.create(lifeTime, instanceTarget.getName() + "@" + mCounter.incrementAndGet());
            ProxyRequestContext ctx = new ProxyRequestContext(task);
            Ref<Thread> ref = new Ref<>();
            handler.post(() -> {
                ref.set(Thread.currentThread());
                //dispatch a cancel call into a thread interrupt, races are handled by the subsequent cancel call
                if (interruptible) {
                    task.addOnCancelledListener(mayInterruptIfRunning -> {
                        if (mayInterruptIfRunning) {
                            ref.get().interrupt();
                        }
                    });
                }
                //early exit, for queued but never executed tasks
                if (ctx.isCancelled()) {
                    task.set(Result.create().putTag(Result.TAG_CANCELLED, null));
                    return;
                }
                try {
                    //implement also support for methods (or in general?) to inject RequestContext
                    Object res = instanceTarget.invoke(instance, args);
                    Result r;
                    if (res instanceof Result) {
                        r = (Result) res;
                        if (ctx.isCancelled()) {
                            r.putTag(Result.TAG_CANCELLED, null);
                        }
                    } else {
                        r = Result.create(res);
                        if (ctx.isCancelled()) {
                            r.putTag(Result.TAG_CANCELLED, null);
                        }

                    }
                    if (callGeneration.get() != myGeneration) {
                        r.putTag(Result.TAG_OUTDATED, null);
                    }
                    task.set(r);
                } catch (InvocationTargetException e) {
                    String sig = Reflection.getName(instance.getClass()) + "." + ifaceMethod.getName();
                    RuntimeException ee = new RuntimeException("connection call failed: " + sig);
                    ee.initCause(e.getTargetException());
                    ee.setStackTrace(trace);
                    Result r = Result.create().setThrowable(ee);
                    if (ctx.isCancelled()) {
                        r.putTag(Result.TAG_CANCELLED, null);
                    }
                    task.set(r);
                } catch (Throwable e) {
                    ExecutionException ee = new ExecutionException(e);
                    ee.setStackTrace(trace);
                    Result r = Result.create().setThrowable(ee);
                    if (ctx.isCancelled()) {
                        r.putTag(Result.TAG_CANCELLED, null);
                    }
                    task.set(r);
                }
            });
            return task;
        }
    }

    private static class MyInvocationHandler implements InvocationHandler {
        private final Object delegate;
        private final Class proxyType;
        private final Map<Method, ConnectionMethod> methods;
        private final Object actualProxy;
        //this is changed for each borrow/return cycle
        private volatile Scope currentScope;

        MyInvocationHandler(Object instance, Class proxyType) {
            this.delegate = instance;
            this.methods = new HashMap<>();
            this.proxyType = proxyType;
            this.actualProxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{proxyType}, this);
            List<Method> availableControllerMethods = Reflection.getMethods(instance.getClass());
            Map<String, Method> availableControllerMethodsLookups = new HashMap<>();
            for (Method m : availableControllerMethods) {
                availableControllerMethodsLookups.put(fingerPrint(m), m);
            }
            for (Method methodToImplement : Reflection.getMethods(proxyType)) {
                String fingerPrint = fingerPrint(methodToImplement);
                Method controllerMethod = availableControllerMethodsLookups.get(fingerPrint);
                if (controllerMethod == null) {
                    methods.put(methodToImplement, new ConnectionMethod(delegate, null, true));
                } else {
                    methods.put(methodToImplement, new ConnectionMethod(delegate, controllerMethod, false));
                }
            }
        }

        /**
         * Creates a fingerprint based on name and param types BUT NOT the return type or exceptions
         */
        private String fingerPrint(Method m) {
            StringBuilder sb = new StringBuilder();
            sb.append(m.getName());
            for (Class c : Reflection.getParameterTypes(m)) {
                sb.append(Reflection.getName(c));
            }
            return sb.toString();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            ConnectionMethod connectionMethod = methods.get(method);
            if (connectionMethod == null) {
                if (method.getName().equals("toString")) {
                    return "connection@" + delegate;
                }
                throw new Panic();
            }
            Handler handler = currentScope.resolveNamedValue(Container.NAME_REQUEST_HANDLER, Handler.class);
            Task<Result<?>> task = connectionMethod.invoke(currentScope, method, handler, args);
            return task;
        }
    }

    private static class ProxyRequestContext implements RequestContext {
        private final SettableTask<?> task;

        ProxyRequestContext(SettableTask<?> task) {
            this.task = task;
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }
}
