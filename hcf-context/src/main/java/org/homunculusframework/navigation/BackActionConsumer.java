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
package org.homunculusframework.navigation;

import org.homunculusframework.factory.container.Container;
import org.homunculusframework.scope.Scope;

/**
 * This default navigation implements a simple stack based navigation approach and resolves
 * everything from a given {@link Scope} and a declared {@link Container}
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface BackActionConsumer {
    /**
     * Goes backwards the navigation (whatever that means). Returns true, if that was possible. The return value does not
     * indicate that the navigation already succeeded, but that it probably will.
     *
     * @return true to indicate that this holder provides a step back and that it tries to apply the prior state.
     * False that the back stack is empty and the default behavior of the callee should apply.
     */
    boolean backward();
}
