package org.homunculusframework.scope;

import org.homunculusframework.concurrent.ExecutionList;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.Handler;
import org.homunculusframework.lang.Procedure;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class SettableTask<T> implements Task<T> {
    private T result;
    private final String key;
    private final Scope scope;

    private SettableTask(Scope scope, String name) {
        this.key = name + "@" + System.identityHashCode(this);
        this.scope = scope;

        scope.putNamedValue(key, new ExecutionList());
    }


    public static <T> SettableTask<T> create(Scope scope, String name) {
        return new SettableTask<>(scope, name);
    }

    @Override
    public void whenDone(Procedure<T> callback) {
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

    @Nullable
    private ExecutionList getExecutionList() {
        ExecutionList list = scope.getNamedValue(key, ExecutionList.class);
        return list;
    }

    public void set(T result) {
        synchronized (this) {
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
                LoggerFactory.getLogger(getClass()).error("cannot call set: main handler is gone");
            }
        }

    }


}
