package org.homunculusframework.factory.scope;

/**
 * In HCF a scope is usually build around a specific bean instance.
 * Scopes which are created for a specific bean may implement this interface to give access to it.
 * <p>
 * Created by Torben Schinke on 19.03.18.
 */

public interface ContextScope<T> extends Scope {
    /**
     * Returns the "main" value of a scope.
     */
    T getContext();
}
