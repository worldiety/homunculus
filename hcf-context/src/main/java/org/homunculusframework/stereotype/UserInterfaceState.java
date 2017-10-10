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
package org.homunculusframework.stereotype;

import java.lang.annotation.*;

/**
 * Indicates that an annotated class is a "UserInterfaceState" (UIS)
 * <p>
 * Definition of "user interface state": Describes a self contained part of the user interface which provides
 * access to a specific functionality or use case. Usually a UIS describes an aggregation of view components within
 * an entire window.
 *
 * @deprecated unclear what the difference is with a widget?
 * @author Torben Schinke
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Deprecated
public @interface UserInterfaceState {
}
