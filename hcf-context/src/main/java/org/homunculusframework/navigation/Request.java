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

import org.homunculusframework.lang.Function;
import org.homunculusframework.lang.Panic;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Represents the request to a resource. Usually this should refer to a {@link org.homunculusframework.stereotype.Controller}
 * annotated backend controller containing a method annotated with {@link org.homunculusframework.factory.annotation.RequestMapping}
 * but it may also refer to another {@link org.homunculusframework.factory.annotation.Widget} (or UIS) directly.
 * Even platform specific resource may be possible and depends on the actual configuration. At the end
 * this mechanism should provide as much freedom as possible.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class Request {
    private final String requestMapping;
    private final Map<String, Object> requestParams;

    public Request(String requestMapping) {
        if (requestMapping.length() == 0) {
            throw new Panic("empty request is not defined");
        }
        if (requestMapping.charAt(0) != '/' || requestMapping.charAt(requestMapping.length() - 1) != '/') {
            StringBuilder normalized = new StringBuilder();
            if (requestMapping.charAt(0) != '/') {
                normalized.append('/');
            }
            normalized.append(requestMapping);
            if (requestMapping.charAt(requestMapping.length() - 1) != '/') {
                normalized.append('/');
            }
            this.requestMapping = normalized.toString();
        } else {
            this.requestMapping = requestMapping;
        }
        this.requestParams = new TreeMap<>();
    }


    /**
     * Returns the requested id, identified by {@link org.homunculusframework.factory.annotation.RequestMapping}
     * in various stereotypes.
     */
    public String getRequestMapping() {
        return requestMapping;
    }

    /**
     * Adds a key and a value. The inserted object MUST be always a defensive copy (or immutable) and SHOULD be
     * serializable (see also {@link org.homunculusframework.factory.annotation.Persistent}). Also the amount
     * of occupied memory should be as small as possible, because depending on the {@link Navigation} this
     * can cause a permanent memory leak.
     */
    public Request put(String key, Object value) {
        requestParams.put(key, value);
        return this;
    }

    /**
     * Walks over the contained parameters until the closure returns false.
     */
    public void forEach(Function<Entry<String, Object>, Boolean> closure) {
        for (Entry<String, Object> entry : requestParams.entrySet()) {
            if (!closure.apply(entry)) {
                return;
            }
        }
    }


}
