package org.homunculusframework.factory.scope;

import org.homunculusframework.lang.Function;
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

    private final List<Scope> children;

    private boolean destroyed;

    public AbsScope() {
        callbacks = new ArrayList<>();
        children = new ArrayList<>();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        destroy();
    }

    private void destroy() {
        //TODO this is not thread safe
        if (destroyed) {
            return;
        }
        destroyed = true;

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

        //defensive copy for child scopes and their destruction
        List<Scope> tmp;
        synchronized (children) {
            tmp = new ArrayList<>(children);
            children.clear();
        }
        for (Scope scope : tmp) {
            scope.onDestroy();
        }

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


    @Override
    public boolean addScope(Scope child) {
        synchronized (children) {
            if (children.contains(child)) {
                return false;
            }
            children.add(child);
            return true;
        }
    }

    @Override
    public boolean removeScope(Scope child) {
        synchronized (children) {
            return children.remove(child);
        }
    }

    @Override
    public void forEachScope(Function<Scope, Boolean> closure) {
        List<Scope> tmp;
        synchronized (children) {
            tmp = new ArrayList<>(children);
        }
        for (Scope scope : tmp) {
            Boolean b = closure.apply(scope);
            if (b == null || !b) {
                return;
            }
        }
    }
}
