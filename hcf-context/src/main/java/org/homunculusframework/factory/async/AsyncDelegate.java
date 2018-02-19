package org.homunculusframework.factory.async;

import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.Handler;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Torben Schinke on 15.02.18.
 */

public abstract class AsyncDelegate<Delegate> {

    @Inject
    private Delegate delegate;

    @Inject
    private Scope scope;


    @Inject
    @Named(Container.NAME_BACKGROUND_HANDLER)
    private Handler handler;


    private final Map<Class, AsyncMethodContext> methods = new IdentityHashMap<>();

    /**
     * Empty constructor used by injection to avoid that extending classes have to repeat the constructor again.
     */
    public AsyncDelegate() {
    }

    protected Delegate getDelegate() {
        return delegate;
    }

    /**
     * See {@link #async(boolean, boolean, Closure)}. The default setting is that interrupts are enabled but pending
     * requests are kept alive.
     *
     * @param closure the closure to execute asynchronously
     * @param <T>     the result
     * @return
     */
    protected <T> Task<Result<T>> async(Closure<Delegate, T> closure) {
        return async(true, false, closure);
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
    protected <T> Task<Result<T>> async(boolean mayInterrupt, boolean cancelPending, Closure<Delegate, T> closure) {
        AsyncMethodContext ctx;
        synchronized (methods) {
            ctx = getContext(closure, mayInterrupt, cancelPending);
        }
        return ctx.invoke(() -> closure.call(getDelegate()));
    }

    private AsyncMethodContext getContext(Closure closure, boolean mayInterrupt, boolean cancelPending) {
        synchronized (methods) {
            AsyncMethodContext ctx = methods.get(closure.getClass());
            if (ctx == null) {
                ctx = new AsyncMethodContext(handler, scope, mayInterrupt, cancelPending);
                methods.put(closure.getClass(), ctx);
            }
            return ctx;
        }
    }


    public interface Closure<In, Out> {
        Out call(In ctr) throws Exception;
    }
}
