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

import org.homunculusframework.concurrent.Task;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;

import java.util.List;

/**
 * An object container has some simple contract methods to start and stop bunches of objects.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface ObjectContainer {

    /**
     * Returns a list of results containing the instance of the according object (or null) and/or the appropriate failure.
     *
     * @param scope the scope to use for creation
     * @return a task
     */
    Task<List<Result<Object>>> start(Scope scope);
}
