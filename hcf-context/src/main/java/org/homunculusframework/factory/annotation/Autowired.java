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
package org.homunculusframework.factory.annotation;

import org.homunculusframework.factory.ObjectCreator;

import java.lang.annotation.*;

/**
 * Marks a constructor, field, setter method or config method as to be automatically injected by HC's
 * dependency injection infrastructure. The concrete capabilities depends on the
 * configured {@link ObjectCreator}.
 * See also {@link org.homunculusframework.factory.component.AFPAutowired}.
 * <p>
 * By default the injection or wiring process will try to match combination of name and type, using the field name.
 * The field name can be overwritten by another value, e.g. to match a value
 * from {@link org.homunculusframework.navigation.ModelAndView}. If no such combination exists, any other matching
 * type is resolved. If that does not succeed the value will be the java default value for that type (e.g. 0 for
 * numbers or null for reference types)
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    /**
     * See {@link Autowired}
     */
    String value() default "";
}
