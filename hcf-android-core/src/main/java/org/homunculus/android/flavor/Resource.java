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
package org.homunculus.android.flavor;

import android.support.annotation.IdRes;

import org.homunculusframework.factory.flavor.hcf.Execute;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
 * <li>def* triple declaration to use {@link android.content.res.Resources#getIdentifier(String, String, String)} if no final integer resource is available (e.g. in libraries)</li>
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
    @IdRes
    int value() default 0;

    /**
     * the type name, e.g. "drawable", see {@link android.content.res.Resources#getIdentifier(String, String, String)}
     */
    String defType() default "";

    /**
     * the resource name, e.g. "my_bitmap", see {@link android.content.res.Resources#getIdentifier(String, String, String)}
     */
    String defName() default "";

    /**
     * the package name, e.g. "com.myapp", see {@link android.content.res.Resources#getIdentifier(String, String, String)}
     */
    String defPackage() default "";
}
