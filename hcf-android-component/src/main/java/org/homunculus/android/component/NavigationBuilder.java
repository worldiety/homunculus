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
package org.homunculus.android.component;

import org.homunculus.android.component.DefaultAndroidNavigation.AndroidNavigation;

/**
 * An specialized navigation for android providing a more fluent navigation API which also incorporates
 * some platform specific characteristics. The name is short, to avoid typing.
 * Created by Torben Schinke on 09.11.17.
 */

public interface NavigationBuilder {

    /**
     * Performs a forward navigation
     *
     * @param type the type
     * @return the navigation builder
     */
    AndroidNavigation forward(Class<?> type);

    /**
     * Performs a forward navigation
     *
     * @param beanName the named bean e.g. by {@link javax.inject.Named}
     * @return the navigation builder
     */
    AndroidNavigation forward(String beanName);


    /**
     * Performs a backward navigation
     *
     * @param type the type
     * @return the navigation builder
     */
    AndroidNavigation backward(Class<?> type);

    /**
     * Performs a backward navigation
     *
     * @param beanName the named bean e.g. by {@link javax.inject.Named}
     * @return the navigation builder
     */
    AndroidNavigation backward(String beanName);


    /**
     * Performs a redirect navigation (not changing the navigation stack)
     *
     * @param type the type
     * @return the navigation builder
     */
    AndroidNavigation redirect(Class<?> type);

    /**
     * Performs a redirect navigation (not changing the navigation stack)
     *
     * @param beanName the named bean e.g. by {@link javax.inject.Named}
     * @return the navigation builder
     */
    AndroidNavigation redirect(String beanName);

}
