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

import org.homunculusframework.factory.executor.Handler;
import org.homunculusframework.factory.executor.MainThread;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated methods are called after before the component is teared down.
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PreDestroy {
    /**
     * Specifies the handler to use for execution. By default the {@link MainThread} is used.
     */
    Class<? extends Handler> in() default MainThread.class;

    /**
     * You can specify multiple methods with an exact execution order. The lower the order value, the earlier it get's executed.
     * If the order is the same for multiple methods, the execution order is undefined.
     */
    int order() default 0;
}
