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
package org.homunculusframework.factory.flavor.hcf;

import org.homunculusframework.factory.connection.Connection;
import org.homunculusframework.factory.container.AnnotatedComponentProcessor;
import org.homunculusframework.factory.flavor.hcf.Widget;
import org.homunculusframework.scope.Scope;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Supports {@link Widget} and returns {@link ComponentType#WIDGET}
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class HCFComponentWidget implements AnnotatedComponentProcessor {
    @Nullable
    @Override
    public <T> AnnotatedComponent<T> process(Scope scope, Class<T> clazz) {
        Widget widget = clazz.getAnnotation(Widget.class);
        if (widget != null) {
            String name = widget.value();
            return new AnnotatedComponent(clazz, name, ComponentType.WIDGET);
        }
        return null;
    }
}
