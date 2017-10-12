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

import org.homunculusframework.lang.Function;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple to use list which stores it's things in a scope and looses all entries automatically when the scope is destroyed.
 * If the scope has been destroyed, the list is cleared and does not accept new entries (no-op, no exceptions).
 * See also {@link ScopeLocal}.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class ScopeList<T> extends AbstractList<T> {

    private final String key;
    private final Scope scope;

    public ScopeList(Scope scope) {
        this("scopeList", scope);
    }

    public ScopeList(String name, Scope scope) {
        this.key = name + "@" + System.identityHashCode(scope);
        this.scope = scope;
        scope.putNamedValue(key, new CopyOnWriteArrayList<>());
    }

    @Override
    public T get(int index) {
        List<T> delegate = getDetachedList();
        if (delegate == null) {
            throw new IndexOutOfBoundsException("scope is gone - index: " + index);
        } else {
            return delegate.get(index);
        }
    }

    @Override
    public int size() {
        List<T> delegate = getDetachedList();
        if (delegate == null) {
            return 0;
        } else {
            return delegate.size();
        }
    }

    @Override
    public void add(int index, T element) {
        List<T> delegate = getDetachedList();
        if (delegate == null) {
            LoggerFactory.getLogger(getClass()).error("cannot add {}, {}: scope is gone", index, element);
        } else {
            delegate.add(index, element);
        }
    }

    @Override
    public T remove(int index) {
        List<T> delegate = getDetachedList();
        if (delegate == null) {
            throw new IndexOutOfBoundsException("scope is gone - index: " + index);
        } else {
            return delegate.remove(index);
        }
    }

    @Nullable
    private List<T> getDetachedList() {
        return scope.getNamedValue(key, List.class);
    }

    /**
     * Loops and provides elements from the list into the closure as long as the closure returns true and elements are
     * available.
     */
    public void forEach(Function<T, Boolean> closure) {
        List<T> delegate = getDetachedList();
        if (delegate != null) {
            for (T val : delegate) {
                if (!closure.apply(val)) {
                    return;
                }
            }
        }
    }

    /**
     * Creates a copy of all list entries.
     */
    public List<T> copy() {
        List<T> delegate = getDetachedList();
        if (delegate != null) {
            return new ArrayList<>(delegate);
        } else {
            return new ArrayList<>();
        }
    }
}
