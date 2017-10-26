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

import java.util.Map.Entry;

/**
 * Introduced to have a differnt map contract which has the following properties:
 * <ul>
 * <li>Consistent across various versions of class frameworks (see Android fragmentation and forEach extensions)</li>
 * <li>Has a correct generic specification</li>
 * <li>perhaps simpler</li>
 * <li>compatible to builder pattern, by relaxing some specifications and relying on return-type overloading</li>
 * </ul>
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface Map<Key, Value> {
    /**
     * Checks if an entry with the given key exists.
     *
     * @param key the key
     * @return true if found, the value may still be null
     */
    boolean has(Key key);

    /**
     * Puts a new key/value pair into the map, replacing any prior value.
     * Important note: the returned value is undefined and may be overloaded by the implementor.
     * This allows contracts to create builder-like patterns or to return any prior value.
     *
     * @param key   the key
     * @param value the value
     * @return undefined result, may be overloaded by a specific implementation
     */
    Object put(Key key, Value value);

    /**
     * Removes the key/value pair from this map.
     * Important note: the returned value is undefined and may be overloaded by the implementor.
     * This allows contracts to create builder-like patterns or to return the removed value.
     *
     * @param key the key
     * @return undefined result, may be overloaded by a specific implementation
     */
    Object remove(Key key);

    /**
     * A common for each loop contract
     *
     * @param closure the closure, returning true as long as looping should happen
     * @return unspecified
     */
    Object forEachEntry(Function<Entry<String, Object>, Boolean> closure);

    /**
     * Puts all entries from the one map into this app
     *
     * @param other the other map
     * @return unspecified
     */
    Object putAll(Map<Key, Value> other);
}
