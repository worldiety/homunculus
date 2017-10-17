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
package org.homunculusframework.factory.container;

import org.homunculusframework.factory.container.AnnotatedComponentProcessor.AnnotatedComponent;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Components (especially controllers) can export methods (with a name). This is how they are identified.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface AnnotatedRequestMapping {
    /**
     * Processes a bunch of methods, when applicable
     */
    @Nullable
    AnnotatedMethod process(Method method);

    class AnnotatedMethod {
        private final Method method;
        private final String name;
        private final Class[] parameterTypes;
        private final String[] parameterNames;

        public AnnotatedMethod(Method method, String name, Class[] parameterTypes, String[] parameterNames) {
            this.method = method;
            this.name = AnnotatedComponent.normalize(name).substring(1);
            this.parameterTypes = parameterTypes;
            this.parameterNames = parameterNames;
        }

        /**
         * Returns a normalized name, so that it can be simply concated with {@link AnnotatedComponent#getName()}
         */
        public String getName() {
            return name;
        }

        public Class[] getParameterTypes() {
            return parameterTypes;
        }

        public String[] getParameterNames() {
            return parameterNames;
        }

        public Method getMethod() {
            return method;
        }

        public String toString() {
            String str = method.getName() + "(";
            for (int i = 0; i < parameterTypes.length; i++) {
                str += parameterTypes[i].getSimpleName();
                if (i < parameterTypes.length - 1) {
                    str += ",";
                }
            }
            str += ")";
            return str;
        }
    }
}
