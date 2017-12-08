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
package org.homunculusframework.concurrent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method in a way that if a new {@link Task} request is made and another call is still pending, the pending call is cancelled
 * automatically. Or otherwise: Intersecting request/response cycles are tagged as {@link org.homunculusframework.lang.Result#TAG_OUTDATED}
 * as usual but also cancelled with the given flag.
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CancelPending {
    /**
     * Default is true. Flags the automatic call to {@link Task#cancel(boolean)}
     *
     * @return if should interrupt
     */
    boolean mayInterruptIfRunning() default true;
}
