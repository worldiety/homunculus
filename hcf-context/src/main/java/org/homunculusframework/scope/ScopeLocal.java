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

import javax.annotation.Nullable;

/**
 * In the broadest sense this is related to a {@link ThreadLocal}. See also {@link ScopeList}
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class ScopeLocal<T> {

    private final String key;
    private final Scope scope;

    public ScopeLocal(Scope scope) {
        this.key = "scopeLocal@" + System.identityHashCode(scope);
        this.scope = scope;
    }

    @Nullable
    public T get() {
        return (T) scope.getNamedValue(key);
    }

    public void set(@Nullable T value) {
        if (!scope.isDestroyed()) {
            scope.putNamedValue(key, value);
        }
    }

}
