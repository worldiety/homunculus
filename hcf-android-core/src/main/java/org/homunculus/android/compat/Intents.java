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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import org.homunculus.android.compat.ActivityEventDispatcher.AbsActivityEventCallback;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.lang.Destroyable;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


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

    public Intents(ActivityEventDispatcher events) {
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

                        res.failResult(TAG_UNACCEPTED_RESULT_CODE);
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
     * See {@link #startIntent(Context, Intent, int...)}. Accepts only {@link Activity#RESULT_OK}
     */
    public final Task<ActivityResult> startIntent(@Nullable Context context, Intent intent) {
        return startIntent(context, intent, Activity.RESULT_OK);
    }

    /**
     * Starts an intent and returns it's result using a task. Consider providing a {@link ContextScope} to avoid leaks
     * through the registered listeners.
     *
     * @param intent            the intent to start. see {@link Activity#startActivityForResult(Intent, int)}
     * @param acceptableResults the acceptable result codes, e.g. {@link Activity#RESULT_OK}, otherwise fails with {@link #TAG_UNACCEPTED_RESULT_CODE}
     * @return
     */
    public final Task<ActivityResult> startIntent(@Nullable Context context, Intent intent, int... acceptableResults) {
        SettableTask<ActivityResult> task = SettableTask.create(ContextScope.getScope(context), "Intents.startIntent");
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

    public Activity getContext() {
        return mActivityEvents.getActivity();
    }

    @Override
    public void destroy() {
        mActivityEvents.unregister(mCallback);
        mActivityIntents.clear();
    }


    public static class ActivityResult {
        private final int[] mAcceptableResultCodes;
        private final Intent mIntentRequest;
        private Intent mIntentResponse;
        private int mResultCode;
        private final int mId;
        private final SettableTask<ActivityResult> mTask;
        private Map<String, Object> mTags = new TreeMap<>();

        ActivityResult(Intent request, int id, int[] acceptableResultCodes, SettableTask<ActivityResult> task) {
            mIntentRequest = request;
            mTask = task;
            mId = id;
            mAcceptableResultCodes = acceptableResultCodes;
        }

        SettableTask<ActivityResult> getTask() {
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
            mTask.set(this);
        }


        void failResult(String tag) {
            mTags.put(tag, null);
            mTask.set(this);
        }
    }


}