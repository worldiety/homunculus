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

/**
 * Provides the possibility to modify a scope before it is used (e.g. by {@link org.homunculusframework.navigation.DefaultNavigation}
 * and {@link Container#prepareScope(Scope)}).
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface ScopePrepareProcessor {
    /**
     * Prepares the given scope.
     *
     * @param configuration the configuration
     * @param scope         the scope to prepare
     */
    void process(Configuration configuration, Scope scope);
}
