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
import android.os.Handler;
import android.os.Looper;

import org.homunculus.android.core.Android;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Configurator;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.flavor.hcf.HomunculusFlavor;
import org.homunculusframework.factory.flavor.hcf.Persistent;

/**
 * Provides annotations and default environment values:
 * <p>
 * <ul>
 * <li>Applies the {@link HomunculusFlavor} which provides {@link Persistent} in the {@link Context#getFilesDir()}/persistent</li>
 * <li>Field injection support for layouts, drawables, texts etc. declared by {@link Resource}</li>
 * <li>{@link Container#NAME_MAIN_HANDLER} using the main thread (default for most things, or if undefined)</li>
 * <li>{@link Container#NAME_BACKGROUND_HANDLER} using 8 threads (for post construct and pre destroy)</li>
 * <li>{@link Container#NAME_REQUEST_HANDLER} using 8 threads (see also {@link org.homunculusframework.factory.container.Binding})</li>
 * <li>{@link Container#NAME_INFLATER_HANDLER} using 8 threads (concurrent view inflation)</li>
 * <li>{@link AndroidScopeContext} which is used by {@link org.homunculusframework.navigation.DefaultNavigation} for UIS.
 * You should always use {@link org.homunculus.android.core.Android#NAME_CONTEXT} to create your views and to allow a proper
 * scoping for sub-components. Do not use your Activity or others by default.</li>
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

        configuration.getRootScope().put(Container.NAME_MAIN_HANDLER, new AndroidMainHandler());
        configuration.getRootScope().put(Android.NAME_MAIN_HANDLER, new Handler(Looper.getMainLooper()));
        configuration.getRootScope().put(Container.NAME_BACKGROUND_HANDLER, new AndroidBackgroundHandler(8, Container.NAME_BACKGROUND_HANDLER, Thread.MIN_PRIORITY));
        configuration.getRootScope().put(Container.NAME_REQUEST_HANDLER, new AndroidBackgroundHandler(8, Container.NAME_REQUEST_HANDLER, Thread.MIN_PRIORITY));
        configuration.getRootScope().put(Container.NAME_INFLATER_HANDLER, new AndroidInflaterHandler(8, Thread.MIN_PRIORITY));
        configuration.addScopePrepareProcessor(new AndroidScopeContext());
    }
}
