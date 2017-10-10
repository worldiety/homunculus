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

import org.homunculusframework.factory.annotation.Widget;

import java.util.Map;
import java.util.TreeMap;

/**
 * The model and view class aggregates a reference to a view (whatever that is, e.g. an Android resource id or an annotated class using
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class ModelAndView {
    private final String view;
    private final Map<String, Object> model;

    public ModelAndView(String view) {
        this.view = view;
        this.model = new TreeMap<>();
    }

    public ModelAndView put(String key, Object value) {
        model.put(key, value);
        return this;
    }

    /**
     * Returns the {@link Widget#value()} to resolve.
     */
    public String getView() {
        return view;
    }
}
