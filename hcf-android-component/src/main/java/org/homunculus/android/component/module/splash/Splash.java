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

import org.homunculus.android.component.R;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.navigation.Navigation;

/**
 * A simple splash view which is intended for extension.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public abstract class Splash {


    private Activity activity;


    private Handler main;

    private Navigation navigation;

    private LayoutInflater inflater;


    public Splash(Activity activity, Handler main, Navigation navigation, LayoutInflater inflater) {
        this.activity = activity;
        this.main = main;
        this.navigation = navigation;
        this.inflater = inflater;
        apply();
    }

    private void apply() {
        activity.setContentView(inflater.inflate(R.layout.hcf_splash, null));

        main.postDelayed(() -> {
            navigation.reset(getTarget());
        }, getDuration());

    }

    /**
     * The target after the time has run out
     */
    protected abstract Binding<?,?,?> getTarget();

    /**
     * The duration in milliseconds
     *
     * @return
     */
    protected long getDuration() {
        return 2000;
    }
}
