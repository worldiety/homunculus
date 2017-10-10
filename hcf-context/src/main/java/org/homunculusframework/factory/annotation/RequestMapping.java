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
package org.homunculusframework.factory.annotation;

import org.homunculusframework.factory.container.Request;

import java.lang.annotation.*;

/**
 * Declares a type or a method with a name space which must be unique all over the system.
 * It is used by the {@link org.homunculusframework.navigation.Navigation} and
 * {@link Request} components.
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    /**
     * A unique id. The uniqueness constraint is applied when concating with the mapping id of the surrounding class (if applied to a method)
     */
    String value();
}
