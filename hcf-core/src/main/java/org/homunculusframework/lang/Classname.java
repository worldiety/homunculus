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

import java.util.IdentityHashMap;

/**
 * The sole purpose of this class is to workaround performance issues on Java platforms like Android, where the
 * class name lookup is fundamentally broken and causes massive I/O for each (and subsequent) calls since Android 5.0.
 * However for some use cases having a fast class name lookup is essential for us.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class Classname {
    //this is not so good (leaking class files) when used in application servers, we can fix that if this will be a use case in the future
    private final static IdentityHashMap<Class, String> names = new IdentityHashMap<>();

    private Classname() {

    }

    /**
     * Returns the name of the entity (class, interface, array class, primitive type, or void) represented by this Class object, as a String.
     * See {@link Class#getName()}.
     */
    public static String getName(Class<?> clazz) {
        synchronized (names) {
            String name = names.get(clazz);
            if (name == null) {
                name = clazz.getName();
                names.put(clazz, name);
            }
            return name;
        }

    }
}
