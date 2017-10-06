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
package org.homunculusframework.lang;

/**
 * The java language itself does not provide a lifecycle contract of an object instance. The finalizer is a private
 * API and changes the VM behavior and is not intended to be invoked by anyone else.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface Destroyable {
    /**
     * Destroys the object and release as much as related resources. Other methods of the object may start
     * to throw {@link IllegalStateException} afterwards. Subsequent calls to destroy must have no effect. The
     * destruction must be synchronous.
     */
    void destroy();
}
