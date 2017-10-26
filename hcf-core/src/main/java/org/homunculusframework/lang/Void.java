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
 * The void-type in java is broken, because every "null" reference (independent of it's type)
 * can be cast to void (which is nothing special in java btw) but you cannot distinguish if you
 * meant really void or just null ("pattern matching"), which is a huge difference, especially when working with generic or
 * reflection (or just instanceof) logic.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class Void {
    public final static Void Value = new Void();

    private Void() {
    }
}
