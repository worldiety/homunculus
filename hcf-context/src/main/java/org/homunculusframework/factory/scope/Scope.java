package org.homunculusframework.factory.scope;

import javax.annotation.Nullable;

/**
 * Created by tschinke on 17.03.18.
 */

public interface Scope {

    /**
     * The parent of the scope, if any
     *
     * @return null or the parent
     */
    @Nullable
    Scope getParent();

    /**
     * Called if the creation of the scope is about to be completed. After this call the scope is considered to be in a valid scope.
     */
    void onCreate();

    /**
     * Called if this scope is about to be destroyed. Before this call the scope is considered to be valid but already while
     * this method runs, it is not.
     */
    void onDestroy();

    /**
     * A scope should usually provide type safe getters but for some kind of logic, e.g. when writing
     * a generic library function, it is not always feasible to bind code to a specific scope and because
     * java has no ducktyping interfaces it can not get written in a better way than this.
     * <p>
     * The given type is tried to be resolved but never created if not found in this scope or any of it's parents.
     *
     * TODO actually this is not true, in a handwritten system we would introduce an interface and implement it in our own scope implementation
     *
     * @param type the type to resolve an instance for
     * @param <T>
     * @return null if any instance if assignable to the given type
     */
    @Nullable
    <T> T resolve(Class<T> type);
}
