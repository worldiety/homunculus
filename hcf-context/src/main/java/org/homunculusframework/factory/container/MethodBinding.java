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


import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;

import javax.annotation.Nullable;

/**
 * A binding to a method which returns an {@link ObjectBinding} of a specific type. This is typically used
 * as another indirection to create an object.
 * <p>
 * The scope of is automatically destroyed after the {@link #onExecute()} returns.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public abstract class MethodBinding<T> extends Binding<ObjectBinding<T>> {

    //a method binding is not expected to have a static constructor
    @Override
    protected void initStatic() {

    }

}
