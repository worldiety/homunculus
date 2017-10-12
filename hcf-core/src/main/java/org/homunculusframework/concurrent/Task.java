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
package org.homunculusframework.concurrent;

import org.homunculusframework.lang.Function;
import org.homunculusframework.lang.Procedure;

/**
 * A really simple Task contract with a specific meaning regarding the callback.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface Task<R> {
    /**
     * Registers a callback which gets always invoked when the task completed (by error, by ignoring execution or by success).
     * The callback thread is undefined and determined by the task environment.
     */
    void whenDone(Procedure<R> res);

    /**
     * Executes in the current main thread but creates a chain with another callable.
     * The callback thread is undefined and determined by the task environment. Probably
     * this is the same as for {@link #whenDone(Procedure)}
     */
    <X> Task<X> continueWith(Function<R, X> callback);
}
