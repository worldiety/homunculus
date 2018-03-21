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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

import org.homunculus.android.core.ActivityCallback;
import org.homunculus.android.core.ActivityEventDispatcher;
import org.homunculus.android.core.ActivityEventDispatcher.AbsActivityEventCallback;
import org.homunculus.android.core.AndroidScopeContext;
import org.homunculus.android.flavor.AndroidMainHandler;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.scope.LifecycleOwner;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.lang.Destroyable;
import org.homunculusframework.lang.Function;
import org.homunculusframework.lang.Procedure;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.SettableTask;

import java.util.ArrayList;
import java.util.List;


/**
 * A helper class to handle Android Intents using an {@link ActivityEventDispatcher}. This gives a developer
 * a more generic API to structure his code, instead of polluting an Activity's base code. In short: be able to write
 * lifecycle aware components. Argumentation is identical to https://developer.android.com/topic/libraries/architecture/lifecycle.html.
 * It also nicely integrates into the world of {@link Scope}s by optionally connecting the lifetime of {@link Task}
 * to a {@link AndroidScopeContext}.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class Intents {
    public final static String TAG_ACTIVITY_NOT_FOUND = "intents.activity.notfound";
    public final static String TAG_UNACCEPTED_RESULT_CODE = "intents.resultcode.unaccepted";

    private final ActivityEventDispatcher dispatcher;
    private final LifecycleOwner lifecycleOwner;

    public Intents(LifecycleOwner lifecycleOwner, ActivityEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.lifecycleOwner = lifecycleOwner;
    }


    /**
     * Just dispatches the call right to the activity
     *
     * @param intent      the intent
     * @param requestCode the more or less unique request code. You should use a hard coded value for each specific call.
     */
    public void startActivityForResult(Intent intent, int requestCode) {
        dispatcher.getActivity().startActivityForResult(intent, requestCode);
    }

    /**
     * Registers the callback which is fired for each response (buffered or not).
     *
     * @param requestCode the request code to
     * @param callback
     */
    public void registerOnActivityResult(int requestCode, Function<ActivityEventDispatcher.ActivityResult, Boolean> callback) {
        AndroidMainHandler.assertMainThread();
        //simply whenReceived for the event
        ActivityCallback cb = new ActivityCallback<>(lifecycleOwner, dispatcher);
        cb.setDelegate(new AbsActivityEventCallback() {
            @Override
            public boolean onActivityResult(Activity activity, int rCode, int resultCode, Intent data) {
                if (requestCode == rCode) {
                    cb.setDelegate(null);
                    return callback.apply(new ActivityEventDispatcher.ActivityResult(activity, requestCode, resultCode, data));
                }
                return false;
            }
        });

        //and also check for an already available buffered result
        ActivityEventDispatcher.ActivityResult res = dispatcher.consumeActivityResult(requestCode);
        if (res != null) {
            callback.apply(res);
        }
    }

    public Activity getContext() {
        return dispatcher.getActivity();
    }


    public interface ResultIntent<R> {
        /**
         * Starts the intent activity for result
         *
         * @return true if the intent has been propagated to the system successfully
         */
        Task<Result<Boolean>> invoke();

        /**
         * Registers for an intent result. This should be registered at the outmost level (e.g. not the button listener) to
         * ensure that it also is called correctly in {@link Activity#onRestoreInstanceState(Bundle)} situations.
         *
         * @param callback the callback to execute after restore instance state or when onActivityResult is just called
         */
        void whenReceived(Procedure<R> callback);
    }


}
