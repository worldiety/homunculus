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

import android.content.Context;
import android.os.Bundle;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.module.uncaughtexception.UncaughtException;
import org.homunculus.android.core.Android;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.navigation.DefaultNavigation;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Just like the {@link EventAppCompatActivity} but provides an even more provided and opinionated configuration allowing
 * an easier bootstrapping, which just works out of the box and guides you towards a working application. It has
 * a default configuration with {@link DefaultAndroidNavigation} and {@link SwitchAnimationLayout}.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public abstract class HomunculusActivity extends EventAppCompatActivity implements UncaughtExceptionHandler {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    /**
     * Intentionally separated from onCreate calls to onProvide a better developer experience for customization purposes
     * e.g. for overriding
     */
    protected void init() {
        DefaultNavigation nav = new DefaultAndroidNavigation(getScope());
        getScope().put(Android.NAME_NAVIGATION, nav);
        getScope().put(Android.NAME_LAYOUT_INFLATER, getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        getScope().put(Android.NAME_FRAGMENT_MANAGER, getFragmentManager());
        nav.forward(create());
    }

    /**
     * Returns the instance of a navigation builder which is a specialized variant of a {@link org.homunculusframework.navigation.Navigation}
     * which is also available for resolving, at by default. Note that this cannot be guaranteed when reconfigured
     * manually.
     *
     * @return the navigation builder, which is null before {@link #onCreate(Bundle)} and after {@link #onDestroy()}
     */
    public NavigationBuilder getNavigation() {
        return getScope().resolve(Android.NAME_NAVIGATION, NavigationBuilder.class);
    }

    @Override
    public void onBackPressed() {
        if (!onDispatchNavigationBackPressed()) {
            super.onBackPressed();
        }
    }

    /**
     * Called to create the first UIS, which is often a kind of splash screen, which also performs
     * some bootstrapping logic, like preparing some more configuration etc.
     *
     * @return the request which creates the first state
     */
    abstract protected Request create();

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        NavigationBuilder nav = getNavigation();
        if (nav != null) {
            nav.redirect(UncaughtException.class).put(UncaughtException.PARAM_THROWABLE, e).start();
        } else {
            LoggerFactory.getLogger(getClass()).error("failed to handle uncaught exception, no navigation present. Uncaught Exception: ", e);
        }
    }
}
