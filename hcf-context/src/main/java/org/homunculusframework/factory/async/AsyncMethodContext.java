package org.homunculusframework.factory.async;

import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.factory.container.Handler;
import org.homunculusframework.factory.container.RequestContext;
import org.homunculusframework.factory.container.UtilStack;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.lang.Ref;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.SettableTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Torben Schinke on 16.02.18.
 */
class AsyncMethodContext {
    private final static AtomicInteger COUNTER = new AtomicInteger();

    //used to detect and flag crossing concurrent invocations -> only the last call is not flagged as outdated
    private final AtomicInteger callGeneration;
    private final boolean interruptible;
    private final boolean cancelPending;
    private final boolean cancelPendingWithInterrupt;
    private SettableTask<?> pendingTask;
    private final Scope lifeTime;
    private final Handler handler;

    AsyncMethodContext(Handler handler, Scope scope, boolean mayInterruptIfRunning, boolean cancelPending) {
        this.callGeneration = new AtomicInteger();
        this.cancelPending = cancelPending;
        this.cancelPendingWithInterrupt = mayInterruptIfRunning;
        this.interruptible = mayInterruptIfRunning;
        this.lifeTime = scope;
        this.handler = handler;
    }

    <Delegate, T> Task<Result<T>> invoke(Callable<T> closure) {


        final int myGeneration = callGeneration.incrementAndGet();
        //capture the synchronous trace
        StackTraceElement[] trace = UtilStack.getCallStack(4);

        final String methodName = trace[0].getMethodName();
        SettableTask<Result<T>> task = SettableTask.create(lifeTime, methodName + "@" + COUNTER.incrementAndGet());
        ProxyRequestContext ctx = new ProxyRequestContext(task);

        //cancel pending, if required, before starting to work
        if (cancelPending) {
            synchronized (this) {
                if (pendingTask != null) {
                    pendingTask.cancel(cancelPendingWithInterrupt);
                }
                pendingTask = task;
            }
        }

        //continue handling job
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
                task.set(Result.<T>create().put(Result.TAG_CANCELLED));
                return;
            }
            try {
                //implement also support for methods (or in general?) to inject RequestContext
                Object res = closure.call();
                Result r;
                if (res instanceof Result) {
                    r = (Result) res;
                    if (ctx.isCancelled()) {
                        r.put(Result.TAG_CANCELLED);
                    }
                } else {
                    r = Result.create(res);
                    if (ctx.isCancelled()) {
                        r.put(Result.TAG_CANCELLED);
                    }

                }
                if (callGeneration.get() != myGeneration) {
                    r.put(Result.TAG_OUTDATED);
                }
                task.set(r);
            } catch (Throwable e) {
                ExecutionException ee = new ExecutionException(e);
                ee.setStackTrace(trace);
                Result r = Result.create().setThrowable(ee);
                if (ctx.isCancelled()) {
                    r.put(Result.TAG_CANCELLED);
                }
                task.set(r);
            }
        });

        return task;
    }

    //TODO this clashes logically with {@link DefaultRequestContext}
    private static class ProxyRequestContext implements RequestContext {
        private final SettableTask<?> task;

        ProxyRequestContext(SettableTask<?> task) {
            this.task = task;
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }

        @Override
        public List<Binding<?, ?>> getReferrer() {
            return new ArrayList<>();
        }
    }
}
