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

import org.homunculusframework.factory.scope.LifecycleOwner;

/**
 * A life cycle callback which is called while the destruction is running. The state of the scope within it's parent is already undefined,
 * and may be already detached.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface OnDestroyCallback {
    void onDestroy(LifecycleOwner owner);
}
