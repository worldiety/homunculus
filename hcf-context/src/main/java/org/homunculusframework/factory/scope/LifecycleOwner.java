package org.homunculusframework.factory.scope;

import org.homunculusframework.scope.OnDestroyCallback;

/**
 * Created by Torben Schinke on 20.03.18.
 */

public interface LifecycleOwner {
    /**
     * Adds the callback
     */
    void addDestroyCallback(OnDestroyCallback cb);

    /**
     * Removes the callback and returns true if it has been removed.
     */
    boolean removeDestroyCallback(OnDestroyCallback cb);
}
