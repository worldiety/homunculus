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

import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.Handler;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.stereotype.Controller;

import java.lang.annotation.*;

/**
 * Denotes a handler name from a scope for execute a class' constructor and destructor and injections.
 * This does not influence the execution of {@link PostConstruct} or {@link PreDestroy}.
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LifecycleHandler {

    /**
     * The name of the {@link Handler} used to create or inflate a Component (e.g. a {@link Controller} or a
     * {@link Widget}). The default for a Controller is inline (== the calling thread) and for a Widget
     * the {@link Container#NAME_MAIN_HANDLER}. Here it may make sense
     * to use the {@link Container#NAME_INFLATER_HANDLER}, e.g. on Android.
     */
    String value();
}
