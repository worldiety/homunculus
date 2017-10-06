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

package org.homunculusframework.scope;

/**
 * A life cycle callback which is called before the destruction has actually started. Everything is still in a valid state.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface OnBeforeDestroyCallback {
    void onBeforeDestroy(Scope scope);
}
