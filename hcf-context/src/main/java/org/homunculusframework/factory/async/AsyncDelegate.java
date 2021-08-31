package org.homunculusframework.factory.async;

import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.container.BackgroundHandler;
import org.homunculusframework.factory.container.Handler;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.lang.Result;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Created by Torben Schinke on 15.02.18.
 */

public abstract class AsyncDelegate<Delegate> {

    protected final static boolean MAY_INTERRUPT = true;
    protected final static boolean DO_NOT_INTERRUPT = false;

    protected final static boolean CANCEL_PENDING = true;
    protected final static boolean DO_NOT_CANCEL_PENDING = false;

    private final Delegate delegate;

    private final Scope scope;


    private final Handler handler;


    private final Map<String, AsyncMethodContext> methods = new IdentityHashMap<>();

    /**
     * Empty constructor used by injection to avoid that extending classes have to repeat the constructor again.
     */
    public AsyncDelegate(Scope scope, BackgroundHandler handler, Delegate delegate) {
        this.delegate = delegate;
        this.scope = scope;
        this.handler = handler;
    }

    protected Delegate getDelegate() {
        return delegate;
    }


    /**
     * Configures the given closure class and invokes it asynchronously. After the first call, changes to the boolean flags have no further effect.
     *
     * @param mayInterrupt  if true, the cancellation of the task will cause an interrupt in the executing thread
     * @param cancelPending if true, subsequent calls to the given closure (-class) will cause a cancellation (and maybe an interrupt) of any task which is still running.
     * @param closure       the closure to execute
     * @param <T>           type of result
     * @return the task with a wrapping {@link Result}. Exceptions are automatically set into {@link Result#getThrowable()}.
     */
    protected <T> Task<Result<T>> async(String methodId, boolean mayInterrupt, boolean cancelPending, Closure<Delegate, T> closure) {
        AsyncMethodContext ctx;
        synchronized (methods) {
            ctx = getContext(methodId, closure, mayInterrupt, cancelPending);
        }
        return ctx.invoke(() -> closure.call(getDelegate()));
    }

    private AsyncMethodContext getContext(String methodId, Closure closure, boolean mayInterrupt, boolean cancelPending) {
        synchronized (methods) {
            AsyncMethodContext ctx = methods.get(methodId);
            if (ctx == null) {
                ctx = new AsyncMethodContext(handler, scope, mayInterrupt, cancelPending);
                methods.put(methodId, ctx);
            }
            return ctx;
        }
    }


    public interface Closure<In, Out> {
        Out call(In ctr) throws Exception;
    }
}
