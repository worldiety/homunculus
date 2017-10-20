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

import android.content.Context;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Configurator;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.flavor.hcf.HomunculusFlavor;
import org.homunculusframework.factory.flavor.hcf.Persistent;

import java.io.File;

/**
 * Provides annotations and default environment values:
 * <p>
 * <ul>
 * <li>Applies the {@link HomunculusFlavor} which provides {@link Persistent} in the {@link Context#getFilesDir()}/persistent</li>
 * <li>Field injection support for layouts, drawables, texts etc. declared by {@link Resource}</li>
 * <li>{@link Container#NAME_MAIN_HANDLER} using the main thread (default for most things, or if undefined)</li>
 * <li>{@link Container#NAME_BACKGROUND_HANDLER} using 8 threads (for post construct and pre destroy)</li>
 * <li>{@link Container#NAME_REQUEST_HANDLER} using 8 threads (see also {@link org.homunculusframework.factory.container.Request})</li>
 * <li>{@link Container#NAME_INFLATER_HANDLER} using 8 threads (concurrent view inflation)</li>
 * </ul>
 */
public class AndroidFlavor implements Configurator {

    private Context context;

    public AndroidFlavor(Context context) {
        this.context = context;
    }

    @Override
    public void apply(Configuration configuration) {

        configuration.addFieldProcessor(new ResourceAnnotationLoader());

        configuration.getRootScope().putNamedValue(Container.NAME_MAIN_HANDLER, new AndroidMainHandler());
        configuration.getRootScope().putNamedValue(Container.NAME_BACKGROUND_HANDLER, new AndroidBackgroundHandler(8, Container.NAME_BACKGROUND_HANDLER, Thread.MIN_PRIORITY));
        configuration.getRootScope().putNamedValue(Container.NAME_REQUEST_HANDLER, new AndroidBackgroundHandler(8, Container.NAME_REQUEST_HANDLER, Thread.MIN_PRIORITY));
        configuration.getRootScope().putNamedValue(Container.NAME_INFLATER_HANDLER, new AndroidInflaterHandler(8, Thread.MIN_PRIORITY));
    }
}
