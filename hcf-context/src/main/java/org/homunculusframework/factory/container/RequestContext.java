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

import java.util.List;

/**
 * A request context can be injected automatically into a controller call, when invoked
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface RequestContext {
    /**
     * Checks if the request has been cancelled.
     *
     * @return true if cancelled.
     */
    boolean isCancelled();

    /**
     * Returns the stack of all referrers which lead to the this request. The last element is the latest referrer.
     * The current implementation allows the direct modification of the stack. However it is still unclear, if this
     * is a good idea or not, because of the asynchronous nature of controller method invocations. So be aware
     * that modifications may cause race conditions (at least logical ones) with your UI.
     * <p>
     * IMPORTANT: Use a post (e.g. an instance of {@link MainHandler} like AndroidMainHandler) to ensure that
     * your UI does not suffer on races, example:
     * <ul>
     * <li>Background-Thread: myController.getMyUISAndModifyStack()</li>
     * <li>Main-Thread: myUIState.onClick() - navigation.getTop() instanceof MyBinding</li>
     * <li>Background-Thread: RequestContext.getReferrer().clear()</li>
     * <li>Main-Thread: MyBinding binding = (MyBinding)navigation.getTop()</li>
     * </ul>
     *
     * @return the actual referrers, changes to it will be propagated to the client.
     */
    List<Binding<?, ?>> getReferrer();
}
