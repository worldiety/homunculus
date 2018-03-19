package org.homunculusframework.factory.scope;

import javax.annotation.Nullable;

/**
 * Created by tschinke on 17.03.18.
 */

public interface Scope {
    Scope getParent();

    void onCreate();

    void onDestroy();

    /**
     * A scope should usually provide type safe getters but for some kind of logic, e.g. when writing
     * a generic library function, it is not always feasible to bind code to a specific scope and because
     * java has no ducktyping interfaces it can not get written in a better way than this.
     * <p>
     * The given type is tried to be resolved but never created if not found in this scope or any of it's parents.
     *
     * @param type the type to resolve an instance for
     * @param <T>
     * @return null if any instance if assignable to the given type
     */
    @Nullable
    <T> T resolve(Class<T> type);
}
