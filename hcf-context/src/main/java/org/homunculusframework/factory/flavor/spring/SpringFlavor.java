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
package org.homunculusframework.factory.flavor.spring;

import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Configurator;

/**
 * Provides support for a subset of spring annotations and behaviors to the {@link Configuration}
 */
public class SpringFlavor implements Configurator {
    @Override
    public void apply(Configuration configuration) {
        configuration.addFieldProcessor(new SPRFieldInject());
        configuration.addComponentProcessor(new SPRComponentController());
        configuration.addRequestMapping(new SPRControllerEndpoint());
    }
}
