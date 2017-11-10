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
package org.homunculus.android.component.module.splash;

import android.app.Activity;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import org.homunculus.android.component.R;
import org.homunculus.android.component.module.uncaughtexception.Reporter;
import org.homunculus.android.flavor.Resource;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.stereotype.UserInterfaceState;


import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * A simple splash view which is intended for extension.
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Named(Splash.NAME)
@UserInterfaceState
public abstract class Splash {
    /**
     * The bean name
     */
    public final static String NAME = "hcf/splash";


    @Inject
    private Activity activity;


    @Inject
    private Handler main;

    @Inject
    private Navigation navigation;

    @Inject
    private LayoutInflater inflater;

    @PostConstruct
    private void apply() {
        activity.setContentView(inflater.inflate(R.layout.hcf_splash, null));

        main.postDelayed(() -> {
            navigation.reset(new Request(getTarget()));
        }, getDuration());

    }

    /**
     * The target after the time has run out
     */
    protected abstract Class<?> getTarget();

    /**
     * The duration in milliseconds
     *
     * @return
     */
    protected long getDuration() {
        return 2000;
    }
}
