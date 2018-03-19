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

import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.factory.scope.ScopedValue;

/**
 * This is just a convenience class to make the purpose of an ObjectBinding clearer.
 *
 * @author Torben Schinke
 * @since 1.0
 */

public abstract class ModelAndView<T, BindResultScope extends Scope & ScopedValue<T>, ParentScope extends Scope & ScopedValue<?>> implements Binding<T, BindResultScope, ParentScope> {
}
