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

import org.homunculusframework.scope.Scope;
import org.homunculusframework.stereotype.UserInterfaceState;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * A component processor must be thread safe and should not hold a state.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface AnnotatedComponentProcessor {
    /**
     * Processes the given type and returns perhaps an annotated component
     */
    @Nullable
    <T> AnnotatedComponent<T> process(Scope scope, Class<T> type);

    class AnnotatedComponent<T> {
        private final Class<T> clazz;
        private final String name;
        private final ComponentType type;

        public AnnotatedComponent(Class<T> clazz, String name, ComponentType type) {
            this.clazz = clazz;
            this.name = normalize(name);
            this.type = type;
        }

        public Class<T> getAnnotatedClass() {
            return clazz;
        }

        public ComponentType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public static String normalize(String in) {
            String normalized = in.trim();
            if (normalized.length() == 0) {
                return "/";
            }
            if (normalized.charAt(0) != '/') {
                normalized = "/" + normalized;
            }
            if (normalized.charAt(normalized.length() - 1) != '/') {
                normalized = normalized + "/";
            }
            return normalized;
        }
    }

    enum ComponentType {
        /**
         * Beans are just pojos but can be also a simple view or an entire UIS.
         * Because there is no exact definition and in reality this may differ between use cases, we do not distinguish
         * them. An UIS can be annotated for documentation purpose using {@link UserInterfaceState}.
         * A widget is typically never created in the root scope.
         */
        BEAN,

        /**
         * A controller ever exists of a single instance per container and is usually created in the root scope.
         * A controller should provide exported synchronous and multithread safe methods.
         */
        CONTROLLER,

    }
}

