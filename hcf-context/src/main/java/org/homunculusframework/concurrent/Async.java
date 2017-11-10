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
package org.homunculusframework.concurrent;

import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.Handler;
import org.homunculusframework.factory.container.RequestContext;
import org.homunculusframework.lang.Function;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * A utility class to provide some convenience ready-to-use-and-do-less-wrong task helper methods.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Async {
    private Async() {

    }

    /**
     * See {@link #inThread(Scope, Function, boolean)}
     */
    public static <T> Task<Result<T>> inThread(Function<RequestContext, Result<T>> closure) {
        return inThread(null, closure, true);

    }

    /**
     * See {@link #inThread(Scope, Function, boolean)}
     */
    public static <T> Task<Result<T>> inThread(Scope scope, Function<RequestContext, Result<T>> closure) {
        return inThread(scope, closure, true);

    }

    /**
     * Just spawns a new thread
     *
     * @param closure      the closure to execute
     * @param mayInterrupt interruptible
     * @param <T>          the return type
     * @return the task
     */
    public static <T> Task<Result<T>> inThread(Scope scope, Function<RequestContext, Result<T>> closure, boolean mayInterrupt) {
        SettableTask<Result<T>> task = SettableTask.create(scope, closure.toString());
        Thread thread = new Thread(closure.toString()) {
            @Override
            public void run() {
                MyRequestContext ctx = new MyRequestContext(task);

                if (mayInterrupt) {
                    ctx.thread = this;
                    task.addOnCancelledListener(mayInterruptIfRunning -> ctx.thread.interrupt());
                }
                Result<T> res;

                try {
                    if (ctx.isCancelled()) {
                        res = Result.create();
                        res.put(Result.TAG_CANCELLED);
                    } else {
                        res = closure.apply(ctx);
                        if (res == null) {
                            res = Result.create();
                        }
                    }
                } catch (Throwable t) {
                    res = Result.create();
                    res.setThrowable(t);
                    if (ctx.isCancelled()) {
                        res.put(Result.TAG_CANCELLED);
                    }
                }
                task.set(res);
            }
        };
        thread.start();
        return task;

    }

    /**
     * Creates a task within the given scope. Destroying the scope may result in cancelling the actual task.
     * The handler used to execute is resolved from the scope using
     * {@link Container#NAME_BACKGROUND_HANDLER}. You should never configure the main thread and interrupt it, to
     * avoid insanity.
     * <p>
     *
     * @param scope   the scope to use
     * @param closure the closure to execute in the future
     * @param <T>     the kind of result
     * @return always a task instance, never null
     */
    public static <T> Task<T> create(Scope scope, Function<RequestContext, T> closure, boolean mayInterrupt) {
        Handler handler = getHandler(scope);
        SettableTask<T> task = SettableTask.create(scope, closure.toString());
        MyRequestContext ctx = new MyRequestContext(task);
        handler.post(() -> {
            if (mayInterrupt) {
                ctx.thread = Thread.currentThread();
                task.addOnCancelledListener(mayInterruptIfRunning -> ctx.thread.interrupt());
            }
            T res;
            try {
                if (ctx.isCancelled()) {
                    res = null;
                } else {
                    res = closure.apply(ctx);
                }
            } catch (Throwable t) {
                LoggerFactory.getLogger(Async.class).error("failed to execute task: ", t);
                res = null;
            }
            task.set(res);
        });
        return task;
    }

    /**
     * Same as {@link #createTask(Scope, Function, boolean)} but does not interrupt, because interrupting is not a safe
     * operation by default for invariants (e.g. NIO based code)
     */
    public static <T> Task<Result<T>> createTask(Scope scope, Function<RequestContext, Result<T>> closure) {
        return createTask(scope, closure, false);
    }

    /**
     * See {@link #create(Scope, Function, boolean)} but inserts tags and exceptions automatically. Result is never null.
     */
    public static <T> Task<Result<T>> createTask(Scope scope, Function<RequestContext, Result<T>> closure, boolean mayInterrupt) {
        Handler handler = getHandler(scope);
        SettableTask<Result<T>> task = SettableTask.create(scope, closure.toString());
        MyRequestContext ctx = new MyRequestContext(task);
        handler.post(() -> {
            if (mayInterrupt) {
                ctx.thread = Thread.currentThread();
                task.addOnCancelledListener(mayInterruptIfRunning -> ctx.thread.interrupt());
            }
            Result<T> res;

            try {
                if (ctx.isCancelled()) {
                    res = Result.create();
                    res.put(Result.TAG_CANCELLED);
                } else {
                    res = closure.apply(ctx);
                    if (res == null) {
                        res = Result.create();
                    }
                }
            } catch (Throwable t) {
                res = Result.create();
                res.setThrowable(t);
                if (ctx.isCancelled()) {
                    res.put(Result.TAG_CANCELLED);
                }
            }
            task.set(res);
        });
        return task;
    }


    /**
     * @deprecated danger of deadlock and hickups. E.g. this will always deadlock when used in the main thread
     * using a task which result depends from main. Anyway, it is ALWAYS(!) wrong to use it in the main thread. If you encounter such
     * situation, your code is just wrong. If you are convinced, that it has to be used, it is a fallacy and you did not understand
     * your actual problem properly! Think about how you would solve it with a backend/frontend separation in a web app.
     * Then partition your problem in a different way and you will never need it.
     * <p>
     * <p>
     * This method will never be removed, because there are always
     * valid situations when to use it. However most(!) situations are badly designed when this is required, so
     * think more than once before using!
     * <p>
     * Implementation notes:
     * <ul>
     * <li>Tries to avoid trivial deadlocks when awaiting a task which is already done (e.g. when already available by main-thread synchronous calculation and {@link SettableTask})</li>
     * </ul>
     */
    @Deprecated
    public static <T> T await(Task<T> task) {
        //avoid blocking in trivial situations and avoid a little deadlock case
        if (task.isDone()) {
            return task.peek();
        }

        MyFutureTask<T> tmp = new MyFutureTask<>(() -> {
            return null;
        });
        task.whenDone(res -> {
            tmp.set(res);
        });

        try {
            return tmp.get();
        } catch (InterruptedException e) {
            throw new Panic(e);
        } catch (ExecutionException e) {
            throw new Panic(e);
        }
    }

    private static class MyFutureTask<T> extends FutureTask<T> {

        public MyFutureTask(Callable<T> callable) {
            super(callable);
        }

        @Override
        protected void set(T t) {
            super.set(t);
        }
    }

    private static Handler getHandler(Scope scope) {
        Handler handler = scope.resolve(Container.NAME_BACKGROUND_HANDLER, Handler.class);
        if (handler == null) {
            //pick any handler
            handler = scope.resolve(Handler.class);
        }
        if (handler == null) {
            throw new Panic("no handler in scope");
        }
        return handler;
    }


    private static class MyRequestContext implements RequestContext {
        private final SettableTask task;
        private volatile Thread thread;

        public MyRequestContext(SettableTask task) {
            this.task = task;
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }
}
