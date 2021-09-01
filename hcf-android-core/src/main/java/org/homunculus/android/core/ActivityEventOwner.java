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
package org.homunculus.android.core;

/**
 * When creating components which requires events or the lifecycle of an activity, you should consider using
 * {@link ActivityEventDispatcher} for it, as long as Android does not support that natively yet. To improve
 * the reusability of your component use this class instead of coding against a concrete activity type like
 * {@link org.homunculus.android.compat.EventAppCompatActivity}.
 * <p>
 * Note: The {@link androidx.lifecycle.Lifecycle} does not support anything else than the simple lifecycle of
 * fragments or activities. Complex cycles like activity results or menus are not possible with it.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface ActivityEventOwner {
    /**
     * Returns the event dispatcher to register or trigger activity events.
     *
     * @return the dispatcher instance
     */
    ActivityEventDispatcher<?> getEventDispatcher();
}
