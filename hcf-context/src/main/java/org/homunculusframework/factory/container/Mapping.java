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

import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Reflection;

import javax.annotation.Nullable;

/**
 * A helper class which represents a reference to a mapping (e.g. a bean or a method of a bean) which can be resolved through a unique api.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Mapping {
    @Nullable
    private final String name;
    @Nullable
    private final Class<?> type;

    private Mapping(@Nullable String name, @Nullable Class<?> type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Creates a bean representation from a name. This has usually to be declared and configured
     *
     * @param name the name of the bean, e.g. as declared by {@link javax.inject.Named}
     * @return a bean instance
     */
    public static Mapping fromName(String name) {
        return new Mapping(name, null);
    }

    /**
     * Creates a bean representation from a type, which needs not to be configured explicitly
     *
     * @param type the type
     * @return a bean instance
     */
    public static Mapping fromClass(Class<?> type) {
        return new Mapping(null, type);
    }

//    /**
//     * Resolves the bean through the container and/or it's configuration.
//     *
//     * @param container the container
//     * @return the resolved type or null if not resolvable (e.g. if not configured)
//     */
//    @Nullable
//    public Class<?> resolve(Container container) {
//        if (type != null) {
//            return type;
//        }
//        return container.getConfiguration().getBeans().get(name);
//    }


    /**
     * The type
     *
     * @return null if not defined or a concrete class
     */
    @Nullable
    public Class<?> getType() {
        return type;
    }

    /**
     * The name
     *
     * @return null if not defined or name identifying a bean or a method within a bean
     */
    @Nullable
    public String getName() {
        return name;
    }

    public MappingType getMappingType() {
        if (name != null) {
            return MappingType.NAME;
        } else {
            return MappingType.CLASS;
        }
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        } else {
            return Reflection.getName(type);
        }
    }

    public enum MappingType {
        NAME,
        CLASS
    }
}
