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

import org.homunculusframework.factory.ProcessingCompleteCallback;
import org.homunculusframework.scope.Scope;

import java.lang.reflect.Method;
import java.util.List;

/**
 * A methods processor must be thread safe and should not hold a state. Applies to a bunch of methods.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface AnnotatedMethodsProcessor {

    /**
     * Processes a bunch of methods, when applicable
     */
    void process(Scope scope, Object instance, List<Method> methods, ProcessingCompleteCallback callback);

}
