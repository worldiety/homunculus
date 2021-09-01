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

import org.homunculusframework.factory.scope.LifecycleOwner;
import org.homunculusframework.lang.Destroyable;
import org.homunculusframework.lang.Reference;

import javax.annotation.Nullable;


/**
 * In the broadest sense this is related to a {@link ThreadLocal}. See also {@link LifecycleList}.
 * The lifetime is tied to that of the scope. If the scope is destroyed, the value is removed.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class LifecycleLocal<T> implements Reference<T>, Destroyable {

    @Nullable
    private T value;
    private LifecycleOwner scope;
    private boolean destroyed;
    private final OnDestroyCallback callback;

    public LifecycleLocal(LifecycleOwner scope) {
        this.scope = scope;
        this.callback = s -> destroy();
        this.scope.addDestroyCallback(callback);
    }

    @Override
    public T get() {
        //update memory barrier
        synchronized (this) {
            return value;
        }
    }

    @Override
    public void set(@Nullable T value) {
        synchronized (this) {
            if (destroyed) {
                return;
            }
            this.value = value;
        }
    }


    /**
     * Sets this value to null and removes it from the scope.
     */
    @Override
    public void destroy() {
        synchronized (this) {
            if (destroyed) {
                return;
            }
            destroyed = true;
            scope.removeDestroyCallback(callback);
            scope = null;
            value = null;
        }
    }
}
