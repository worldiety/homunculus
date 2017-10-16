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
package org.homunculusframework.factory.flavor.ee;

import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Configurator;

/**
 * Provides a subset of various JSRs. Support is as follows:
 * <p>
 * JSR-330:
 * <ul>
 * <li>dependency injection with {@link javax.inject.Inject} and {@link javax.inject.Named}</li>
 * <li>Controllers are identified using {@link javax.inject.Singleton}</li>
 * </ul>
 * <p>
 * JSR-250:
 * <ul>
 * <li>lifecycle support using {@link javax.annotation.PostConstruct} and {@link javax.annotation.PreDestroy}</li>
 * </ul>
 * <p>
 * JSR 311:
 * <ul>
 * <li>Annotate controller endpoints using the REST style and @Path and @FormParam annotations</li>
 * </ul>
 */
public class EEFlavor implements Configurator {
    @Override
    public void apply(Configuration configuration) {
        configuration.addFieldProcessor(new FieldInject());
        configuration.addMethodSetupProcessors(new MethodsPostConstruct());
        configuration.addMethodSetupProcessors(new MethodsPreDestroy());
    }
}
