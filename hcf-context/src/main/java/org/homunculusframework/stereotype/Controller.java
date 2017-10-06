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
 * Indicates that an annotated class is a "Controller" (e.g. an app controller).
 * <p>
 * Indicates that an annotated class is a "Controller" and therefore is considered as a candidate for auto-detection
 * when using annotation-based configuration and classpath scanning.
 * A "Controller" is always treated as a singelton and must be "stateless".
 * Each request from the UI (user interface) to the "Controller" must contain all of the information necessary to
 * understand the request, and cannot take advantage of any stored context on the server.
 * Session state is therefore kept entirely on the UI. You always have to take multiple UIs (or clients)
 * into account and respect concurrent calls on all endpoint methods.
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
}
