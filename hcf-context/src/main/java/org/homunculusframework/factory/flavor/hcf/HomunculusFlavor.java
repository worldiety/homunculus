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

import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Configurator;

import java.io.File;

/**
 * The default hcf flavor contains those elements, which are unique to the platform:
 * <p>
 * <ul>
 * <li>Loading and saving simple object trees for a field using {@link Persistent}</li>
 * </ul>
 */
public class HomunculusFlavor implements Configurator {

    private File privateStorageDirectory;

    public HomunculusFlavor(File privateStorageDirectory) {
        this.privateStorageDirectory = privateStorageDirectory;
    }

    @Override
    public void apply(Configuration configuration) {
        configuration.addFieldProcessor(new HCFFieldPersistent(privateStorageDirectory));
        configuration.addComponentProcessor(new HCFComponentCtrConnection());
        configuration.addComponentProcessor(new HCFComponentWidget());
    }
}
