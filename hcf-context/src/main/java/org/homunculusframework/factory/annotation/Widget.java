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

import java.lang.annotation.*;

/**
 * Denotes a view component to be displayed on screen. Annotate your view class with it and
 * refer to it using {@link org.homunculusframework.navigation.Request}. Use a request with
 * {@link org.homunculusframework.navigation.Navigation} to automatically create a stack based user
 * flow.
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Widget {
    /**
     * The unique id to identify the view or component.
     */
    String value();
}
