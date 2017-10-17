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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * The sole purpose of this class is to workaround performance issues on Java platforms like Android, where the
 * class name lookup is fundamentally broken and causes massive I/O for each (and subsequent) calls since Android 5.0.
 * However for some use cases having a fast class name lookup is essential for us.
 *
 * @author Torben Schinke
 * @since 1.0
 */
final class Clazz {
    //this is not so good (leaking class files) when used in application servers, we can fix that if this will be a use case in the future
    private final static IdentityHashMap<Class, String> names = new IdentityHashMap<>();
    private final static IdentityHashMap<Class, List<Field>> fields = new IdentityHashMap<>();

    private Clazz() {

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

    /**
     * Returns all declared fields and returns them from a non-defensive copy cache. Fields of the super class are last, otherwise fields are in reflective order (not in any particular order)
     */
    public static List<Field> getFields(Class clazz) {
        synchronized (fields) {
            List<Field> res = fields.get(clazz);
            if (res == null) {
                res = new ArrayList<>();
                Class root = clazz;
                while (root != null) {
                    for (Field m : root.getDeclaredFields()) {
                        res.add(m);
                    }
                    root = root.getSuperclass();
                }
                fields.put(clazz, res);
            }

            return res;
        }

    }
}
