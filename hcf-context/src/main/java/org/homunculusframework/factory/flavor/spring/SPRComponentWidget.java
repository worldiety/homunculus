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
package org.homunculusframework.factory.flavor.spring;

import org.homunculusframework.factory.container.AnnotatedComponentProcessor;
import org.homunculusframework.scope.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Supports {@link Component} and returns {@link ComponentType#BEAN}
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class SPRComponentWidget implements AnnotatedComponentProcessor {
    @Nullable
    @Override
    public <T> AnnotatedComponent<T> process(Scope scope, Class<T> clazz) {
        Component widget = clazz.getAnnotation(Component.class);
        if (widget != null) {
            return new AnnotatedComponent(clazz, widget.value(), ComponentType.BEAN);
        }
        return null;
    }
}
