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

import org.homunculusframework.factory.container.AnnotatedRequestMapping;
import org.homunculusframework.lang.Reflection;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Supports {@link Named} methods as exported methods complemented by {@link Named} for parameters.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class EEControllerEndpoint implements AnnotatedRequestMapping {
    @Nullable
    @Override
    public AnnotatedMethod process(Method method) {
        Named named = method.getAnnotation(Named.class);
        if (named != null) {
            Class[] parameterTypes = Reflection.getParameterTypes(method);
            String[] parameterNames = new String[parameterTypes.length];

            AnnotatedMethod res = new AnnotatedMethod(method, named.value(), parameterTypes, parameterNames);
            Annotation[][] declaredParameterAnnotations = Reflection.getParameterAnnotations(method);
            NEXT_PARAM:
            for (int i = 0; i < declaredParameterAnnotations.length; i++) {
                Annotation[] annotations = declaredParameterAnnotations[i];
                for (Annotation annotation : annotations) {
                    if (annotation instanceof Named) {
                        String name = ((Named) annotation).value();
                        parameterNames[i] = name;
                        continue NEXT_PARAM;
                    }
                }
                //if we come here, no annotation was found -> not good!
                LoggerFactory.getLogger(method.getDeclaringClass()).error("{}.{}: {}. Parameter is unnamed and may cause invocation failure at runtime ", method.getDeclaringClass().getSimpleName(), res, i);
            }
            return res;
        }
        return null;
    }
}
