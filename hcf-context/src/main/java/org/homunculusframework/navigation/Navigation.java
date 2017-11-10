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

import org.homunculusframework.factory.container.Request;

import javax.annotation.Nullable;

import java.util.List;

/**
 * This navigation contract models a stack navigation and provides a stack history in which each forward or backward modifies the stack.
 * See also {@link DefaultNavigation} as standard implementation with such behavior.
 * <p>
 * <pre>
 *     /uis4 -> getTop() or getStack().size() - 1 or reload()
 *     /uis3 -> getPriorTop() or getStack().size() - 2 or backward()
 *     /uis2
 *     /uis1
 * </pre>
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface Navigation {

    /**
     * Resets the stack, applies the request and afterwards the request is the sole entry on the stack
     *
     * @param request the request
     */
    void reset(Request request);

    /**
     * Goes forward the navigation (whatever that means)
     */
    void forward(Request request);

    /**
     * Goes backwards the navigation (whatever that means). Returns true, if that was possible. The return value does not
     * indicate that the navigation already succeeded. The current request is discarded and the prior request is issued again.
     */
    boolean backward();

    /**
     * Goes backwards to a specific request (matching the {@link Request#getMapping()} value), discarding all unmatched requests.
     * If no such request is found, the stack is empty and the request is added and applied. So, the request is always applied.
     * The parameters of the given target request are applied on top of the found request.
     *
     * @param request the request
     */
    void backward(Request request);

    /**
     * Just executes and applies the given request, without modifying the current stack. This is especially useful
     * to create "stackless" navigation states e.g. to handle error conditions.
     *
     * @param request the request
     */
    void redirect(Request request);


    /**
     * Reloads whatever is current at the top of the stack ({@link #getTop()})
     *
     * @return true if something is reloadable
     */
    boolean reload();

    /**
     * Pops the last request from the navigation stack and returns it. Does not apply anything.
     *
     * @return the last element in the stack which may be the currently visible state (but for subsequent calls this must not be the case)
     */
    @Nullable
    Request pop();

    /**
     * Pushes another request on top of the navigation stack without applying anything.
     *
     * @param request the new stack entry
     */
    void push(Request request);


    /**
     * Returns the top most stack top/tail value. This is not necessarily the current active request
     *
     * @return the current
     */
    @Nullable
    Request getTop();

    /**
     * The currently active request. Keep an eye on logical races. While creating/destroying the actual current state is not defined
     * and up to the concrete implementation.
     *
     * @return the currently applied request (stable only when not pending anymore)
     */
    @Nullable
    Request getCurrent();

    /**
     * Returns the request right before the top most entry.
     *
     * @return the entry before the top
     */
    @Nullable
    Request getPriorTop();

    /**
     * Returns the current backing stack, which can be modified directly. The stack top is the tail (length - 1) of the list.
     * Watch out for logical races! However an implementation has to ensure (e.g. through blocking or copying) that
     * the behavior in general is thread safe. The current active request is always at the top/tail.
     *
     * @return the stack, never null
     */
    List<Request> getStack();
}
