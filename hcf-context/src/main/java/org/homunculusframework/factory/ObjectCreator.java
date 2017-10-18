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
package org.homunculusframework.factory;

import org.homunculusframework.scope.Scope;

/**
 * @author Torben Schinke
 * @since 1.0
 */
public interface ObjectCreator {
    /**
     * Tries to create a new instance of the given type using the given scope to resolve
     * all required dependencies. It is always a programming error when this call fails.
     */
    <T> T create(Scope scope, Class<T> type) throws FactoryException;
}