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

import org.homunculusframework.lang.Destroyable;
import org.homunculusframework.lang.Function;
import org.homunculusframework.lang.Reflection;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

/**
 * A scope provides the fundamental information about all existing instances in a context. A scope is always part
 * of a hierarchy and has no cycles but siblings and children (subScopes). The implementation is thread safe, however that
 * does not mean it is logically safe when used. By definition a scope has the ownership of {@link Destroyable} values.
 * If you don't want that for specific values, you have to remove such values manually before destroying. After destroyed,
 * most methods do nothing and act like an empty and detached scope. Also inserting values are ignored.
 * <p>
 * By convention, named framework specific members are prefixed with $.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class Scope implements Destroyable {
    /**
     * Each scope has a unique name
     */
    private final String name;
    /**
     * A scope always has a parent, but the root scope itself, when it's null.
     * Dependencies are always resolved bottom-up the scope tree.
     */
    @Nullable
    private Scope parent;
    /**
     * Children scopes are always directly addressable to avoid linear search regressions in corner cases with lot's of subScopes.
     */
    private final Map<String, Scope> subScopes;

    /**
     * Named variables are kinds of directly addressable slots in the scope. Think of variable names in a block (=scope).
     * Using the name as a lookup improves performance in various cases.
     */
    private final Map<String, Object> namedValues;


    //the hidden lock to make the scope operations thread safe
    private final Object lock = new Object();

    //some use cases require a specific behavior to react properly on the destruction cycle
    private final List<OnBeforeDestroyCallback> dcbBefore;

    //some use cases require a specific behavior to react properly on the destruction cycle
    private final List<OnAfterDestroyCallback> dcbAfter;

    //flag to detect developer brain failures
    private boolean destroyed;


    public Scope(String name, @Nullable Scope parent) {
        this.name = name;
        this.parent = parent;
        this.subScopes = new TreeMap<>();
        this.namedValues = new TreeMap<>();
        this.dcbAfter = new ArrayList<>(3);
        this.dcbBefore = new ArrayList<>(3);
        if (parent != null) {
            parent.subScopes.put(name, this);
        }
    }

    /**
     * Adds the callback. Note: You can also implement {@link Destroyable} instead of registering for the lifecycle event.
     */
    public void addOnBeforeDestroyCallback(OnBeforeDestroyCallback cb) {
        synchronized (lock) {
            dcbBefore.add(cb);
        }
    }

    /**
     * Removes the callback and returns true if it has been removed.
     */
    public boolean removeOnBeforeDestroyCallback(OnBeforeDestroyCallback cb) {
        synchronized (lock) {
            return dcbBefore.remove(cb);
        }
    }

    /**
     * Adds the callback. Note: You can also implement {@link Destroyable} instead of registering for the lifecycle event.
     */
    public void addOnAfterDestroyCallback(OnAfterDestroyCallback cb) {
        synchronized (lock) {
            dcbAfter.add(cb);
        }
    }

    /**
     * Removes the callback and returns true if it has been removed.
     */
    public boolean removeOnAfterDestroyCallback(OnAfterDestroyCallback cb) {
        synchronized (lock) {
            return dcbAfter.remove(cb);
        }
    }

    private boolean printDestroyedWarning(String msg) {
        if (destroyed) {
            LoggerFactory.getLogger(getClass()).warn("{}.{}: is already destroyed", name, msg);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the (unique in relation to it's siblings within it's parent) name of this scope
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the parent scope, which is only null if the root scope has been reached.
     */
    @Nullable
    public Scope getParent() {
        synchronized (lock) {
            printDestroyedWarning("getParent()");
            return parent;
        }
    }

    /**
     * Returns the named sub scope or child scope, if available.
     */
    @Nullable
    public Scope getScope(String name) {
        synchronized (lock) {
            printDestroyedWarning("getScope()");
            return subScopes.get(name);
        }
    }

    /**
     * Puts the given scope, returning any prior scope with the same name. The prior scope is not destroyed.
     */
    @Nullable
    public Scope attach(Scope scope) {
        synchronized (lock) {
            if (printDestroyedWarning("attach()")) {
                return null;
            }
            Scope other = subScopes.put(scope.getName(), scope);
            if (other != null) {
                synchronized (other.lock) {
                    other.parent = null;
                }
            }
            scope.parent = this;
            return other;
        }
    }

    /**
     * Removes the denoted scope without destroying it.
     */
    @Nullable
    public Scope detach(String name) {
        synchronized (lock) {
            printDestroyedWarning("detach()");
            Scope scope = subScopes.remove(name);
            if (scope != null) {
                //detach the parent of child
                synchronized (scope.lock) {
                    scope.parent = null;
                }
            }
            return scope;
        }
    }

    /**
     * To differentiate between null values and absent values this method has been introduced.
     */
    public boolean hasNamedValue(String name) {
        synchronized (lock) {
            printDestroyedWarning("hasNamedValue()");
            return namedValues.containsKey(name);
        }
    }

    /**
     * To differentiate between null values and absent values this method has been introduced.
     * Walks up the parents, if any of them defines something like that.
     */
    public boolean hasResolvableNamedValue(String name) {
        synchronized (lock) {
            printDestroyedWarning("hasResolveableNamedValue()");
            if (!namedValues.containsKey(name) && parent != null) {
                return parent.hasResolvableNamedValue(name);
            }
            return namedValues.containsKey(name);
        }
    }

    /**
     * To differentiate between null values and absent values this method has been introduced.
     */
    public boolean hasNamedValue(String name, Class<?> type) {
        synchronized (lock) {
            printDestroyedWarning("hasNamedValue()");
            Object obj = namedValues.get(name);
            if (obj != null && type.isAssignableFrom(obj.getClass())) {
                return true;
            }
            return false;
        }
    }

    /**
     * To differentiate between null values and absent values this method has been introduced.
     * Walks up the parents, if any of them defines something like that and tries to duck type it.
     */
    public boolean hasResolvableNamedValue(String name, Class<?> type) {
        synchronized (lock) {
            printDestroyedWarning("hasResolvableNamedValue()");
            if (!namedValues.containsKey(name) && parent != null) {
                return parent.hasResolvableNamedValue(name, type);
            }
            Object obj = namedValues.get(name);
            Object res = Reflection.castDuck(obj, type);
            if (obj != null && res == null) {
                return false;
            }
            if (obj == null && res != null) {
                return false;
            }
            if (obj != null && res != null) {
                //well we can't be sure (e.g. casting "abc" to int will return "0"?
                return true;
            }

            return false;
        }
    }

    /**
     * A convenience helper method. Returns the named value and tries to cast it.
     * If not possible, returns null, as it would have been never defined. See also {@link #getNamedValue(String)}.
     * The logic performs a duck type and tries to make some reasonable casts or type conversions, e.g. between string and numbers.
     */
    @Nullable
    public <T> T getNamedValue(String name, Class<T> type) {
        synchronized (lock) {
            printDestroyedWarning("getNamedValue()");

            Object obj = namedValues.get(name);
            return (T) Reflection.castDuck(obj, type);
        }
    }

    /**
     * Similar to {@link #getNamedValue(String, Class)} but resolves the variable also by looking into the parents.
     * Each child of a parent may shadow the parents variable with the same name.
     * The logic performs a duck type and tries to make some reasonable casts or type conversions, e.g. between string and numbers.
     */
    public <T> T resolveNamedValue(String name, Class<T> type) {
        synchronized (lock) {
            printDestroyedWarning("resolveNamedValue()");

            if (!namedValues.containsKey(name) && parent != null) {
                return parent.resolveNamedValue(name, type);
            }

            Object obj = namedValues.get(name);
            return (T) Reflection.castDuck(obj, type);
        }

    }

    /**
     * Just returns the named value, returning null if unknown or null.
     */
    @Nullable
    public Object getNamedValue(String name) {
        synchronized (lock) {
            printDestroyedWarning("getNamedValue()");
            return namedValues.get(name);
        }
    }

    /**
     * Just removes a named value and returns it.
     */
    @Nullable
    public Object removeNamedValue(String name) {
        synchronized (lock) {
            printDestroyedWarning("removeNamedValue()");
            return namedValues.remove(name);
        }
    }

    /**
     * Puts a named values and returns any existing value.
     */
    @Nullable
    public Object putNamedValue(String name, Object value) {
        synchronized (lock) {
            if (printDestroyedWarning("putNamedValue()")) {
                return null;
            }
            return namedValues.put(name, value);
        }
    }


    /**
     * Walks over the entire scope hierarchy to resolve the type. The resolution order is defined as following (bottom-up):
     * <ol>
     * <li>Scope.class is mapped to this instance</li>
     * <li>Try to assign from named declarations</li>
     * <li>Ask the parent to resolve</li>
     * </ol>
     */
    @Nullable
    public <T> T resolve(Class<T> type) {
        synchronized (lock) {
            printDestroyedWarning("resolve()");
            if (type == Scope.class) {
                return (T) this;
            }
        }

        //2.
        for (Object value : namedValues.values()) {
            if (type.isAssignableFrom(value.getClass())) {
                return (T) value;
            }
        }

        //3.
        if (parent != null) {
            return parent.resolve(type);
        }
        return null;

    }

    /**
     * Checks if the scope is destroyed. Checkout for logical races, in concurrent use cases.
     */
    public boolean isDestroyed() {
        synchronized (lock) {
            return destroyed;
        }
    }

    /**
     * Destroys and invokes all registered life cycle methods. This also removes this scope from it's parent.
     * The life cycle is defined as follows:
     * <ol>
     * <li>Invoke all registered {@link OnBeforeDestroyCallback}s</li>
     * <li>Remove all before callbacks</li>
     * <li>Try to destroy all named values</li>
     * <li>Remove all named values</li>
     * <li>Detach from parent</li>
     * <li>Invoke all registered {@link OnAfterDestroyCallback}s</li>
     * <li>Remove all after callbacks</li>
     * </ol>
     * <p>
     * Important side note: The destruction process is tried to be done completely before any
     * (runtime) exception is bubbled up.
     */
    @Override
    public void destroy() {
        synchronized (lock) {
            if (destroyed) {
                return;
            }

            //destroy all children first, we copy them here, because each child detaches itself causing concurrentmodificationexceptions
            for (Scope child : new ArrayList<>(subScopes.values())) {
                child.destroy();
            }

            destroyed = true;
            //1.
            //avoid GC for Android when we can
            for (int i = 0; i < dcbBefore.size(); i++) {
                dcbBefore.get(i).onBeforeDestroy(this);
            }
            dcbBefore.clear();


            //3.
            for (Object value : namedValues.values()) {
                if (value instanceof Destroyable) {
                    ((Destroyable) value).destroy();
                }
            }
            namedValues.clear();

            //4.
            if (parent != null) {
                parent.detach(getName());
            }

            //5.
            //avoid GC for Android when we can
            for (int i = 0; i < dcbAfter.size(); i++) {
                dcbAfter.get(i).onAfterDestroy(this);
            }
            dcbAfter.clear();
        }
    }


    /**
     * Loops over all child scopes, as long as the closure returns true
     */
    public void forEachScope(Function<Scope, Boolean> closure) {
        synchronized (lock) {
            printDestroyedWarning("forEachScope()");
            for (Scope scope : subScopes.values()) {
                if (!closure.apply(scope)) {
                    return;
                }
            }
        }
    }

    /**
     * Loops over all entries (non-recursive), as long as the closure returns true
     */
    public void forEachEntry(Function<Entry<String, Object>, Boolean> closure) {
        synchronized (lock) {
            printDestroyedWarning("forEachEntry()");
            for (Entry<String, Object> entry : namedValues.entrySet()) {
                if (!closure.apply(entry)) {
                    return;
                }
            }
        }
    }
}
