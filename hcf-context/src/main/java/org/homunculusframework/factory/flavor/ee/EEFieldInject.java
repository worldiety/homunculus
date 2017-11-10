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
package org.homunculusframework.factory.flavor.ee;

import org.homunculusframework.concurrent.Async;
import org.homunculusframework.factory.container.AnnotatedFieldProcessor;
import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

import java.lang.reflect.Field;

/**
 * Uses {@link javax.inject.Inject} and optionally {@link Named}. If no {@link Named} is annotated, any type of any scope (or it's parents)
 * is matched, otherwise only those with the same name.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class EEFieldInject implements AnnotatedFieldProcessor {

    @Override
    public void process(Scope scope, Object instance, Field field) {
        javax.inject.Inject autowired = field.getAnnotation(javax.inject.Inject.class);
        Named named = field.getAnnotation(Named.class);
        if (autowired != null) {
            Object resolvedValue;
            if (named != null) {
                resolvedValue = scope.resolve(named.value(), field.getType());
            } else {
                resolvedValue = scope.resolve(field.getType());
            }
            //check if null and not resolvable -> try to create such an instance
            if (resolvedValue == null && named == null) {
                Container container = scope.resolve(Container.NAME_CONTAINER, Container.class);
                if (container != null) {
                    //await has danger of deadlocks, especially for PostConstructs in main thread (which is the default case)
                    resolvedValue = Async.await(container.createComponent(scope, field.getType())).get();
                }
            }

            field.setAccessible(true);
            try {
                field.set(instance, resolvedValue);
                LoggerFactory.getLogger(instance.getClass()).info("{}.{} = {}", instance.getClass().getSimpleName(), field.getName(), DefaultFactory.stripToString(resolvedValue));
            } catch (IllegalAccessException e) {
                LoggerFactory.getLogger(instance.getClass()).error("failed: {}.{} -> {}", instance.getClass().getSimpleName(), field.getName(), e);
            }
        }
    }
}
