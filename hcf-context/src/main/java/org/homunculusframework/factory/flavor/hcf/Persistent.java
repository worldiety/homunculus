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

import org.homunculusframework.factory.serializer.Serializable;
import org.homunculusframework.factory.serializer.Serializer;
import org.homunculusframework.factory.serializer.Xml;

import java.lang.annotation.*;

/**
 * Marks a field or getter method to save the declared model "before" exiting
 * the scope which was used to create the instance. It is guaranteed that if nothing can be deserialized, the actual
 * value is null (however a reference is never null).
 * <p>
 * If you annotate a generic {@link org.homunculusframework.lang.Reference} the setter will perform a manual
 * synchronous save. Due to the contract, exceptions are mapped either as runtime exceptions or are ignored.
 * It is guaranteed that the Reference instance is never null. Also keep in mind never to replace it with a custom
 * instance of a reference.
 * <p>
 * Consider to use a name, especially when used with primitives.
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Persistent {

    /**
     * By default the persistence is a singelton-like behavior regarding the specific type.
     */
    String name() default "";

    /**
     * The default serializer is the Xml serializer which trade-offs speed vs compatibility. The serializer must be
     * an exact match and cannot be satisfied with assignability of the class hierarchy.
     */
    Class<? extends Serializer> serializer() default Serializable.class;
}
