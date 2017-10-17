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
package org.homunculusframework.factory.container;

import org.homunculusframework.scope.Scope;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A component result used to communicate the state of container treatment.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Component<T> {
    @Nullable
    private final T value;
    private final List<Throwable> failures;
    private final Scope scope;

    public Component(Scope scope, T value, List<Throwable> failures) {
        this.value = value;
        this.failures = failures;
        this.scope = scope;
    }

    public Component(Scope scope, T value, Throwable failure) {
        this.value = value;
        this.failures = new ArrayList<>();
        this.failures.add(failure);
        this.scope = scope;
    }

    /**
     * Returns the component or null if creation failed. If not null, you should inspect
     * {@link #getFailures()} if you want to decide to use the result or not. Sometimes it may
     * be perfectly fine to ignore certain errors.
     */
    @Nullable
    public T get() {
        return value;
    }

    /**
     * Returns associated failures, usually occured while creating or injecting.
     */
    public List<Throwable> getFailures() {
        return failures;
    }

    /**
     * Returns the scope in which the component has been created
     */
    public Scope getScope() {
        return scope;
    }
}
