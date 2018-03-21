package org.homunculusframework.factory.scope;

import org.homunculusframework.scope.OnDestroyCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tschinke on 17.03.18.
 */

public abstract class AbsScope implements Scope {

    //the hidden lock to make the scope operations thread safe
    private final Object lock = new Object();

    private final List<OnDestroyCallback> callbacks;


    public AbsScope() {
        callbacks = new ArrayList<>();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        destroy();
    }

    private void destroy() {
        //defensive copy again, to protect against weired stuff
        final ArrayList<OnDestroyCallback> tmpOnBeforeDestroyCallbacks;
        synchronized (callbacks) {
            tmpOnBeforeDestroyCallbacks = new ArrayList<>(callbacks);
            callbacks.clear();
        }
        for (int i = 0; i < tmpOnBeforeDestroyCallbacks.size(); i++) {
            tmpOnBeforeDestroyCallbacks.get(i).onDestroy(this);
        }
        tmpOnBeforeDestroyCallbacks.clear();

    }

    @Override
    public void addDestroyCallback(OnDestroyCallback cb) {
        synchronized (callbacks) {
            callbacks.add(cb);
        }
    }

    @Override
    public boolean removeDestroyCallback(OnDestroyCallback cb) {
        synchronized (callbacks) {
            return callbacks.remove(cb);
        }
    }


}
