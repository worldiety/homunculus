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

import java.io.Serializable;

/**
 * Represents a decoupled binding to a bean. The binding prepares and performs the creation of the actual
 * object and also it's surrounding scope, which is the thing returned. A binding shall fulfill the following
 * requirements:
 * <ul>
 * <li>A binding may be executed asynchronously</li>
 * <li>A binding can refer to injectable fields and/or a constructor of an object, depending on the implementation only package private or public fields are supported</li>
 * <li>A binding can refer to a method</li>
 * <li>The creation of a bean using the binding is thread safe</li>
 * </ul>
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface Binding<Bean, BindResultScope extends Scope & ScopedValue<Bean>, ParentScope extends Scope & ScopedValue<?>> extends Serializable {

    /**
     * Executes this binding, which typically means at least one object creation
     *
     * @param scope the scope to apply
     * @return the result
     */
    BindResultScope create(ParentScope scope) throws Exception;
}
