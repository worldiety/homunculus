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
package org.homunculus.android.compat;

import android.app.Application;
import android.content.Context;
import org.homunculus.android.core.Android;
import org.homunculus.android.core.ContextScope;
import org.homunculus.android.flavor.AndroidFlavor;
import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.flavor.ee.EEFlavor;
import org.homunculusframework.factory.flavor.hcf.HomunculusFlavor;
import org.homunculusframework.factory.flavor.spring.SpringFlavor;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.scope.Scope;

import java.io.File;

/**
 * Just provides an application with a {@link ContextScope} by overloading {@link android.content.ContextWrapper#attachBaseContext(Context)}
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class CompatApplication extends Application {

    private Scope mAppScope;

    @Override
    protected void attachBaseContext(Context base) {
        if (mAppScope != null) {
            mAppScope.destroy();
        }
        mAppScope = new Scope("/", null);
        mAppScope.put(Android.NAME_CONTEXT, this);
        ContextScope ctx = new ContextScope(mAppScope, base);
        super.attachBaseContext(ctx);
    }

    /**
     * Creates a default configuration. The configuration always uses the scope of the application context.
     * Also the returned configuration is app-wide and should not provide leaky things to an activity (besides children scopes).
     * By default this provides a fully useable and reasonable out-of-the-box configuration for Android, providing
     * spring and ee annotation flavors.
     */
    protected Configuration createConfiguration() {
        Scope appScope = ContextScope.getScope(this);
        if (appScope == null) {
            throw new Panic("application is not correctly configured: ApplicationContext must provide a ContextScope (e.g. use CompatApplication)");
        }
        Configuration configuration = new Configuration(appScope);

        File dir = new File(this.getFilesDir(), "hcf");

        new AndroidFlavor(this).apply(configuration);
        new HomunculusFlavor(new File(getFilesDir(), "persistent")).apply(configuration);
        new EEFlavor().apply(configuration);
        new SpringFlavor().apply(configuration);

        //configure the factories
        DefaultFactory defaultFactory = new DefaultFactory(configuration);
        configuration.setObjectCreator(defaultFactory);
        configuration.setObjectInjector(defaultFactory);
        configuration.setObjectDestroyer(defaultFactory);


        return configuration;
    }

    /**
     * Returns the application scope
     */
    public Scope getScope() {
        return mAppScope;
    }
}
