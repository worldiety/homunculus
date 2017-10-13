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
package org.homunculus.android.annotation;

import org.homunculus.android.core.ResourceAnnotationLoader;
import org.homunculusframework.factory.annotation.Execute;

import java.lang.annotation.*;

/**
 * An Android resource annotation refers to basically any kind of resource which tries to automatically inflate/load/decode
 * the resource and converts it into the according field.
 * <p>
 * Currently supported (see {@link ResourceAnnotationLoader})
 * <ul>
 * <li>R.drawable for drawable fields</li>
 * <li>R.drawable for bitmap fields</li>
 * <li>R.string for Strings</li>
 * <li>R.layout for Views (see also {@link Execute} to influence the inflation thread)</li>
 * </ul>
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Resource {

    /**
     * The android resource id
     */
    int value();
}
