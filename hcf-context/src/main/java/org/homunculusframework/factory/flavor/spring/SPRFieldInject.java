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

import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.factory.container.AnnotatedFieldProcessor;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Named;
import java.lang.reflect.Field;

/**
 * Uses {@link javax.inject.Inject} and optionally {@link Named}. If no {@link Named} is annotated, any type of any scope (or it's parents)
 * is matched, otherwise only those with the same name.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class SPRFieldInject implements AnnotatedFieldProcessor {

    @Override
    public void process(Scope scope, Object instance, Field field) {
        Autowired autowired = field.getAnnotation(Autowired.class);
        Qualifier named = field.getAnnotation(Qualifier.class);
        if (autowired != null) {
            Object resolvedValue;
            if (named != null) {
                resolvedValue = scope.resolveNamedValue(named.value(), field.getType());
            } else {
                resolvedValue = scope.resolve(field.getType());
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
