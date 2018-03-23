package org.homunculusframework.factory.scope;

import org.homunculusframework.lang.Function;
import org.homunculusframework.scope.OnDestroyCallback;

import javax.annotation.Nullable;

/**
 * Created by tschinke on 17.03.18.
 */

public interface Scope extends LifecycleOwner {

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
     * <p>
     * Important: Scope.class is always resolved to this.
     * <p>
     * TODO actually this is not true, in a handwritten system we would introduce an interface and implement it in our own scope implementation
     *
     * @param type the type to resolve an instance for
     * @param <T>
     * @return null if any instance if assignable to the given type
     */
    @Nullable
    <T> T resolve(Class<T> type);

    /**
     * Adds another scope as child
     *
     * @param child
     * @return true if successfully added
     */
    boolean addScope(Scope child);

    /**
     * Removes the given scope from the children list.
     *
     * @param child
     * @return true if successfully removed
     */
    boolean removeScope(Scope child);

    /**
     * Loops over the direct children (not recursive) until no other child is available or false is returned.
     * Scopes are not considered to be entries which are resolvable, see also {@link #forEachEntry(Function)}
     *
     * @param closure the closure to call
     */
    void forEachScope(Function<Scope, Boolean> closure);


    /**
     * Loops over all direct scope entries (not recursive, neither up or done). Does not loop over children scopes.
     *
     * @param closure
     */
    void forEachEntry(Function<Object, Boolean> closure);
}
