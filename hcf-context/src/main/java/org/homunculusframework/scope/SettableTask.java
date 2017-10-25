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
package org.homunculusframework.scope;

import org.homunculusframework.concurrent.ExecutionList;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.Handler;
import org.homunculusframework.lang.Function;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Procedure;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link Task} which is optionally connectable to a scope.
 * When created in the context of a {@link Scope}, the callbacks are removed automatically
 * to avoid (especially view-) leaks through inner classes or similar constructs. Automatically
 * cancels when going out of scope and result is not known yet.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class SettableTask<T> implements Task<T> {
    private volatile T result;
    private final String key;
    @Nullable
    private final Scope scope;
    private final ExecutionList leakyExecutionList;
    private volatile boolean shouldCancel;
    private volatile boolean shouldCancelWithInterrupt;
    private volatile boolean done;
    private final List<OnCancelledListener> onCancelledListeners = new ArrayList<>(1);
    private final OnBeforeDestroyCallback beforeDestroyCallback;

    private SettableTask(Scope scope, String name) {
        this.key = name + "@" + System.identityHashCode(this);
        this.scope = scope;

        if (scope != null) {
            this.beforeDestroyCallback = s -> cancel(true);
            scope.putNamedValue(key, new ExecutionList());
            leakyExecutionList = null;
        } else {
            this.beforeDestroyCallback = null;
            leakyExecutionList = new ExecutionList();
        }
    }

    public void addOnCancelledListener(OnCancelledListener listener) {
        synchronized (onCancelledListeners) {
            onCancelledListeners.add(listener);
        }
    }

    @Nullable
    @Override
    public T peek() {
        return result;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    /**
     * Creates a task without any bound scope. This is prone to leaks, if used in the UI, so think about
     * using {@link #create(Scope, String)}
     */
    public static <T> SettableTask<T> create(String name) {
        return create(null, name);
    }

    /**
     * Creates a settable task which can be bound to a scope to couple the life time of listeners to the life time
     * of the scope. Doing so is always a good idea when working with a UI.
     */
    public static <T> SettableTask<T> create(@Nullable Scope scope, String name) {
        return new SettableTask<>(scope, name);
    }

    @Override
    public void whenDone(Procedure<T> callback) {
        if (leakyExecutionList != null) {
            leakyExecutionList.add(() -> callback.apply(result));
        } else {
            if (scope == null) {
                throw new Panic();
            }
            Handler handler = scope.resolveNamedValue(Container.NAME_MAIN_HANDLER, Handler.class);
            if (handler != null) {
                handler.post(() -> {
                    ExecutionList list = getExecutionList();
                    if (list != null) {
                        list.add(() -> callback.apply(result));
                    }
                });
            } else {
                LoggerFactory.getLogger(getClass()).error("cannot call whenDone: main handler is gone");
            }
        }

    }

    @Nullable
    private ExecutionList getExecutionList() {
        ExecutionList list = scope.getNamedValue(key, ExecutionList.class);
        return list;
    }

    /**
     * Sets a result to this task. Subsequent calls are ignored. Works also without {@link Scope} or {@link Handler}.
     * Expects by default {@link Container#NAME_MAIN_HANDLER}
     */
    public void set(T result) {
        synchronized (this) {
            if (done) {
                return;
            }
            done = true;
            this.result = result;
            if (leakyExecutionList != null) {
                leakyExecutionList.execute();
            } else {
                synchronized (this) {
                    if (scope == null) {
                        throw new Panic();
                    } else {
                        scope.removeOnBeforeDestroyCallback(beforeDestroyCallback);
                    }
                    this.result = result;
                    Handler handler = scope.resolveNamedValue(Container.NAME_MAIN_HANDLER, Handler.class);
                    if (handler != null) {
                        handler.post(() -> {
                            ExecutionList list = getExecutionList();
                            if (list != null) {
                                list.execute();
                            }
                        });
                    } else {
                        ExecutionList list = getExecutionList();
                        if (list != null) {
                            list.execute();
                        }
                    }
                }
            }
        }
    }

    @Override
    public <X> Task<X> continueWith(Function<T, X> callback) {
        SettableTask<X> delayedRes = new SettableTask<>(scope, "continueWith-" + key);
        whenDone(res -> {
            X otherRes = callback.apply(res);
            delayedRes.set(otherRes);
        });
        return delayedRes;
    }

    /**
     * @param mayInterruptIfRunning
     */
    @Override
    public void cancel(boolean mayInterruptIfRunning) {
        if (beforeDestroyCallback != null && scope != null) {
            scope.removeOnBeforeDestroyCallback(beforeDestroyCallback);
        }
        if (shouldCancel) {
            return;
        }
        shouldCancel = true;
        shouldCancelWithInterrupt = mayInterruptIfRunning;
        synchronized (onCancelledListeners) {
            for (int i = 0; i < onCancelledListeners.size(); i++) {
                onCancelledListeners.get(i).onCancelled(mayInterruptIfRunning);
            }
        }

    }

    public boolean isCancelled() {
        return shouldCancel;
    }

    public boolean isInterrupted() {
        return shouldCancelWithInterrupt;
    }


    public interface OnCancelledListener {
        void onCancelled(boolean mayInterruptIfRunning);
    }
}
