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

import org.homunculusframework.factory.container.Mapping;
import org.homunculusframework.lang.Function;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * The model and view class aggregates a reference to a view (whatever that is, e.g. an Android resource id or an annotated class using
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class ModelAndView {
    private final Mapping mapping;
    private final Map<String, Object> model;

    /**
     * Creates a model and view
     *
     * @param view
     */
    public ModelAndView(String view) {
        this.mapping = Mapping.fromName(view);
        this.model = new TreeMap<>();
    }

    public ModelAndView(Class<?> view) {
        this.mapping = Mapping.fromClass(view);
        this.model = new TreeMap<>();
    }

    /**
     * Puts another key / value pair into the model, replacing any prior registered value for the given key.
     *
     * @param key   the key
     * @param value the value, should be small and serializable (or a defensive copy). Never put observers, callbacks, listeners, threads etc. here
     * @return the same instance
     */
    public ModelAndView put(String key, Object value) {
        model.put(key, value);
        return this;
    }

    /**
     * Walks over the contained parameters until the closure returns false.
     */
    public void forEach(Function<Entry<String, Object>, Boolean> closure) {
        for (Entry<String, Object> entry : model.entrySet()) {
            if (!closure.apply(entry)) {
                return;
            }
        }
    }

    /**
     * Returns the view mapping to resolve
     */
    public Mapping getView() {
        return mapping;
    }
}
