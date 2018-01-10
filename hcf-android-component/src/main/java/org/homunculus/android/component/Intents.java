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

import org.homunculus.android.core.ActivityEventDispatcher;
import org.homunculus.android.core.ActivityEventDispatcher.AbsActivityEventCallback;
import org.homunculus.android.core.ActivityEventDispatcher.ActivityResult;
import org.homunculus.android.core.ContextScope;
import org.homunculus.android.flavor.AndroidMainHandler;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.lang.Destroyable;
import org.homunculusframework.lang.Function;
import org.homunculusframework.lang.Procedure;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;

import java.util.ArrayList;
import java.util.List;


/**
 * A helper class to handle Android Intents using an {@link ActivityEventDispatcher}. This gives a developer
 * a more generic API to structure his code, instead of polluting an Activity's base code. In short: be able to write
 * lifecycle aware components. Argumentation is identical to https://developer.android.com/topic/libraries/architecture/lifecycle.html.
 * It also nicely integrates into the world of {@link Scope}s by optionally connecting the lifetime of {@link Task}
 * to a {@link ContextScope}.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class Intents implements Destroyable {
    public final static String TAG_ACTIVITY_NOT_FOUND = "intents.activity.notfound";
    public final static String TAG_UNACCEPTED_RESULT_CODE = "intents.resultcode.unaccepted";

    private final ActivityEventDispatcher mActivityEvents;
    private final AbsActivityEventCallback mCallback;
    private final List<ActivityResult> mActivityIntents;
    private final Scope mScope;

    public Intents(Scope scope, ActivityEventDispatcher events) {
        mScope = scope;
        mActivityEvents = events;
        mActivityIntents = new ArrayList<>();
        mCallback = new AbsActivityEventCallback() {
            @Override
            public boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                for (ActivityResult res : mActivityIntents) {
                    if (res.getId() == requestCode) {
                        for (Integer resCode : res.getAcceptableResultCodes()) {
                            if (resCode == resultCode) {
                                res.setResult(data, resultCode);
                                mActivityIntents.remove(res);
                                return true;
                            }
                        }

                        mActivityIntents.remove(res);
                        return true;
                    }
                }
                return false;
            }
        };
        mActivityEvents.register(mCallback);
    }


    /**
     * Starts an intent and returns it's result using a task. Consider providing a non-null scope to avoid leaks
     * through the registered listeners.
     *
     * @param intent            the intent to start. see {@link Activity#startActivityForResult(Intent, int)}
     * @param acceptableResults the acceptable result codes, e.g. {@link Activity#RESULT_OK}, otherwise fails with {@link #TAG_UNACCEPTED_RESULT_CODE}
     * @return
     * @deprecated this invocation will miss all results which arrive after the activity is re-created, e.g. if the camera app would cause a low memory situation. Use {@link #startActivityForResult(Intent, int)} and {@link #registerOnActivityResult(int, Function)} instead.
     */
    @Deprecated
    public final Task<Result<ActivityResult>> startIntent(Intent intent, int... acceptableResults) {
        SettableTask<Result<ActivityResult>> task = SettableTask.create(mScope, "Intents.startIntent");
        final int requestCode = ActivityEventDispatcher.generateNextRequestId();
        final ActivityResult activityResult = new ActivityResult(intent, requestCode, acceptableResults, task);
        try {
            mActivityIntents.add(activityResult);
            mActivityEvents.getActivity().startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            activityResult.failResult(TAG_ACTIVITY_NOT_FOUND);
            mActivityIntents.remove(activityResult);
        }
        return task;
    }

    /**
     * Just dispatches the call right to the activity
     *
     * @param intent      the intent
     * @param requestCode the more or less unique request code. You should use a hard coded value for each specific call.
     */
    public void startActivityForResult(Intent intent, int requestCode) {
        mActivityEvents.getActivity().startActivityForResult(intent, requestCode);
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
        mActivityEvents.register(mScope, new AbsActivityEventCallback() {
            @Override
            public boolean onActivityResult(Activity activity, int rCode, int resultCode, Intent data) {
                if (requestCode == rCode) {
                    return callback.apply(new ActivityEventDispatcher.ActivityResult(activity, requestCode, resultCode, data));
                }
                return false;
            }
        });

        //and also check for an already available buffered result
        ActivityEventDispatcher.ActivityResult res = mActivityEvents.consumeActivityResult(requestCode);
        if (res != null) {
            callback.apply(res);
        }
    }

    public Activity getContext() {
        return mActivityEvents.getActivity();
    }

    @Override
    public void destroy() {
        mActivityEvents.unregister(mCallback);
        mActivityIntents.clear();
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


    /**
     * Only used by {@link #startIntent(Intent, int...)}, use {@link ActivityEventDispatcher.ActivityResult} instead,
     * because the state recovery after an activity restart is not possible (e.g. the listeners)
     */
    @Deprecated
    public static class ActivityResult {
        private final int[] mAcceptableResultCodes;
        private final Intent mIntentRequest;
        private Intent mIntentResponse;
        private int mResultCode;
        private final int mId;
        private final SettableTask<Result<ActivityResult>> mTask;

        ActivityResult(Intent request, int id, int[] acceptableResultCodes, SettableTask<Result<ActivityResult>> task) {
            mIntentRequest = request;
            mTask = task;
            mId = id;
            mAcceptableResultCodes = acceptableResultCodes;
        }

        SettableTask<Result<ActivityResult>> getTask() {
            return mTask;
        }

        int getId() {
            return mId;
        }

        public Intent getRequest() {
            return mIntentRequest;
        }

        public Intent getResponse() {
            return mIntentResponse;
        }

        public int getResultCode() {
            return mResultCode;
        }

        int[] getAcceptableResultCodes() {
            return mAcceptableResultCodes;
        }

        void setResult(Intent response, int resultCode) {
            mResultCode = resultCode;
            mIntentResponse = response;
            Result<ActivityResult> result = Result.create(this);

            mTask.set(result);
        }

        void failResult(String tag) {
            Result<ActivityResult> result = Result.create();
            result.put(tag);
            mTask.set(result);
        }

    }


}
