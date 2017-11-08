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

import java.lang.annotation.*;

/**
 * Marks a method to be NOT safe to use with {@link Thread#interrupt()}. Background: not all interrupts are safe but
 * violate invariants, e.g. NIO based I/O is not safe to use with interrupts because channels are closed automatically
 * and throw {@link java.nio.channels.ClosedByInterruptException} which is usually not what the caller expects (e.g.
 * when working with a database using NIO and interrupting a query, resulting in a closed db, is not the expected result).
 * A developer can mark a controller's endpoint method to avoid interruptions from tasks.
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ThreadNotInterruptible {
}
