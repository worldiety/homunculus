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

import org.homunculusframework.factory.container.AnnotatedRequestMapping;
import org.homunculusframework.lang.Reflection;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Supports {@link org.springframework.web.bind.annotation.RequestMapping} methods as exported methods complemented by {@link org.springframework.web.bind.annotation.RequestParam} for parameters.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class SPRControllerEndpoint implements AnnotatedRequestMapping {
    @Nullable
    @Override
    public AnnotatedMethod process(Method method) {
        RequestMapping named = method.getAnnotation(RequestMapping.class);
        if (named != null) {
            Class[] parameterTypes = Reflection.getParameterTypes(method);
            String[] parameterNames = new String[parameterTypes.length];
            String name = "";
            if (named.value().length > 0) {
                name = named.value()[0];
            } else {
                name = method.getName();
            }
            AnnotatedMethod res = new AnnotatedMethod(method, name, parameterTypes, parameterNames);
            Annotation[][] declaredParameterAnnotations = Reflection.getParameterAnnotations(method);
            NEXT_PARAM:
            for (int i = 0; i < declaredParameterAnnotations.length; i++) {
                Annotation[] annotations = declaredParameterAnnotations[i];
                for (Annotation annotation : annotations) {
                    if (annotation instanceof RequestParam) {
                        String pName = ((RequestParam) annotation).value();
                        parameterNames[i] = pName;
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
