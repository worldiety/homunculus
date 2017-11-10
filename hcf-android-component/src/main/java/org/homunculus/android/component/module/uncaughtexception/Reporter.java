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
package org.homunculus.android.component.module.uncaughtexception;

import org.homunculusframework.concurrent.Task;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;

import java.util.Map;

/**
 * A simple reporter contract for incidents of undefined kinds.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface Reporter {

    /**
     * Creates a report asynchronously and returns a ticket id / code or contact. May fail due to various reasons.
     *
     * @param crashData additional data to submit
     */
    Task<Result<String>> report(Scope scope, Map<String, Object> crashData);
}
