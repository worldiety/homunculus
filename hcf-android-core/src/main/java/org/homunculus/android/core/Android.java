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
package org.homunculus.android.core;


import android.app.Activity;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import org.homunculusframework.factory.component.*;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.navigation.DefaultNavigation;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;

import javax.annotation.Nullable;
import java.io.File;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A simple bootstrap helper class to setup HCF in an android environment. To make the usage of HC components simpler
 * and working out-of-the-box we create some framework code which makes some assumption for Android takes some decisions.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Android {

    /**
     * The name for a {@link Navigation}
     */
    public final static String NAME_NAVIGATION = "$navigation";
    /**
     * The name for a {@link Context}
     */
    public final static String NAME_CONTEXT = "$context";

    /**
     * Holding our scope structures. It does not leak because the new {@link LifecycleOwner} of android will protect us.
     */
    private static final Map<LifecycleOwner, Scope> sAndroidScopes = new IdentityHashMap<>();
    private static Scope sAppContextScope;

    private Android() {
    }

    /**
     * Returns the app wide configuration as a singelton. The configuration always uses {@link #getRootScope()}.
     * Also the returned configuration is app-wide and should not provide leaky things to an activity.
     * By default this provides a fully useable and reasonable out-of-the-box configuration for Android.
     */
    public static Configuration getConfiguration(Context context) {
        Configuration configuration = new Configuration(getRootScope());
        DefaultFactory defaultFactory = new DefaultFactory();
        defaultFactory.addFieldProcessor(new AFPAutowired());
        defaultFactory.addFieldProcessor(new AFPPersistent(new File(context.getFilesDir(), "persistent")));
        defaultFactory.addFieldProcessor(new ResourceAnnotationLoader());
        defaultFactory.addMethodSetupProcessors(new AMPPostConstruct());
        defaultFactory.addMethodTearDownProcessors(new AMPPreDestroy());

        configuration.setObjectCreator(defaultFactory);
        configuration.setObjectInjector(defaultFactory);
        configuration.setObjectDestroyer(defaultFactory);

        return configuration;
    }

    public static void startContainer(Context context) {

    }

    /**
     * Always uses the {@link #getRootScope()} if the owner denotes a {@link Context}. See also {@link #getScope(Scope, LifecycleOwner)}
     */
    public static Scope getScope(LifecycleOwner lifecycleOwner) {
        return getScope(getRootScope(), lifecycleOwner);
    }

    /**
     * Returns a scope for the given lifecycle owner (e.g. a Fragment or an Activity) and registers
     * it statically. The returned scope is created the first time it is accessed. Note: The lifetime is
     * attached to the {@link android.arch.lifecycle.Lifecycle} AND the parent scope. So the returned
     * scope is destroyed if either of them is destroyed.
     */
    public static Scope getScope(@Nullable Scope parent, LifecycleOwner lifecycleOwner) {
        synchronized (sAndroidScopes) {
            Scope scope = sAndroidScopes.get(lifecycleOwner);
            if (scope == null) {
                scope = new Scope(lifecycleOwner.toString(), parent);
                lifecycleOwner.getLifecycle().addObserver(new LifecycleObserver() {
                    @OnLifecycleEvent(Event.ON_DESTROY)
                    void onDestroy() {
                        Scope scope;
                        synchronized (sAndroidScopes) {
                            scope = sAndroidScopes.remove(lifecycleOwner);
                        }
                        if (scope != null) {
                            scope.destroy();
                        }
                    }
                });
                if (lifecycleOwner instanceof Context) {
                    scope.putNamedValue(NAME_CONTEXT, lifecycleOwner);
                }
                if (lifecycleOwner instanceof Activity) {
                    scope.putNamedValue(NAME_NAVIGATION, new DefaultNavigation(scope));
                }
                sAndroidScopes.put(lifecycleOwner, scope);
            }
            return scope;
        }
    }


    /**
     * Similar to {@link #getScope(LifecycleOwner)} but always returns the application scope
     * (which has no lifecycle but until today probably never needs any on Android). This is also the same scope
     * which the {@link #getConfiguration(Context)#getRootScope(Context)} returns.
     */
    public static Scope getRootScope() {
        if (sAppContextScope != null) {
            return sAppContextScope;
        }
        synchronized (sAndroidScopes) {
            if (sAppContextScope == null) {
                sAppContextScope = new Scope("/", null);
                sAppContextScope.putNamedValue(Container.NAME_MAIN_HANDLER, new AndroidMainHandler());
                sAppContextScope.putNamedValue(Container.NAME_BACKGROUND_HANDLER, new AndroidBackgroundHandler(8, Container.NAME_BACKGROUND_HANDLER, Thread.MIN_PRIORITY));
                sAppContextScope.putNamedValue(Container.NAME_REQUEST_HANDLER, new AndroidBackgroundHandler(8, Container.NAME_REQUEST_HANDLER, Thread.MIN_PRIORITY));
                sAppContextScope.putNamedValue(Container.NAME_INFLATER_HANDLER, new AndroidInflaterHandler(8, Thread.MIN_PRIORITY));
            }
            return sAppContextScope;
        }
    }
}
