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

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.homunculus.android.core.ActivityCallback;
import org.homunculus.android.core.ActivityEventDispatcher;
import org.homunculus.android.core.ActivityEventDispatcher.AbsActivityEventCallback;
import org.homunculus.android.core.ActivityEventDispatcher.ActivityEventCallback;
import org.homunculus.android.core.AndroidScopeContext;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.lang.Destroyable;
import org.homunculusframework.lang.Result;
import org.homunculusframework.scope.SettableTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A helper class to write permission aware components. Task callbacks are optionally bound to {@link Scope}
 * by using {@link AndroidScopeContext}. Don't forget to declare the correct permissions in your manifest.
 * <p>
 * Android 6 packs permissions into groups, but these groups is just another ill-defined set. Even google says that we should not use it at all. Taken from the doc:
 * <p>
 * "Your app still needs to explicitly request every permission it needs, even if the user has already granted another permission in the same group.
 * In addition, the grouping of permissions into groups may change in future Android releases. Your code should not rely on the assumption that particular permissions are or are not in the same group."
 * Also note that only "dangerous" permissions can be influenced by the user, see https://developer.android.com/guide/topics/security/permissions.html#normal-dangerous
 * <p>
 * Because Google has such a bad compatibility API design, we will provide our own type safe logic here
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Permissions implements Destroyable {
    public final static String TAG_PERMISSION_DENIED = "permission.denied";

    private final Scope mScope;
    private final ActivityEventDispatcher mEventDispatcher;
    private final ActivityEventCallback mCallback;
    private final List<RequestHolder> mFeatureRequests;

    /**
     * Creates a new permission instance, which registers to the event dispatcher
     *
     * @param scope           the scope to bind the life time and task life to
     * @param eventDispatcher the dispatcher to use
     */
    public Permissions(Scope scope, ActivityEventDispatcher<?> eventDispatcher) {
        mScope = scope;
        mEventDispatcher = eventDispatcher;
        mFeatureRequests = new ArrayList<>();
        mCallback = new AbsActivityEventCallback() {
            @Override
            public void onActivityRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
                for (RequestHolder holder : mFeatureRequests) {
                    if (requestCode == holder.id) {
                        for (int i = 0; i < permissions.length; i++) {
                            String permission = permissions[i];
                            int grantResult = grantResults[i];

                            holder.updateResponse(permission, grantResult);
                        }
                        holder.task.set(Arrays.asList(holder.responses));

                        mFeatureRequests.remove(holder);
                        return;
                    }
                }
            }
        };
        new ActivityCallback<>(scope,eventDispatcher).setDelegate(mCallback);
    }


    /**
     * A ready to use aggregation of {@link ContextCompat#checkSelfPermission(Context, String)} and
     * {@link ActivityCompat#requestPermissions(Activity, String[], int)}
     *
     * @param permissions the permissions e.g. from {@link permission}. Permissions may be also empty.
     * @return a task with a list of all requested permissions
     */
    public Task<List<PermissionResponse>> handlePermissions(String... permissions) {
        RequestHolder holder = new RequestHolder(mScope, permissions);
        mFeatureRequests.add(holder);

        //start checking, a subset may already have been granted
        List<String> missingPermissions = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int state = ContextCompat.checkSelfPermission(getActivity(), permission);
            holder.responses[i] = new PermissionResponse(permission, state);
            if (holder.responses[i].isDenied()) {
                missingPermissions.add(permission);
            }
        }

        if (missingPermissions.isEmpty()) {
            //android has a non-documented behavior here: missingPermissions is empty: java.lang.IllegalArgumentException: permission cannot be null or empty
            //the PermissionResponse has been updated just above, and can be returned
            holder.task.set(Arrays.asList(holder.responses));
        } else {
            //now request the missing permissions, the response runs over the holder
            ActivityCompat.requestPermissions(getActivity(), missingPermissions.toArray(new String[missingPermissions.size()]), holder.id);
        }

        return holder.task;
    }

    /**
     * A shortcut version of {@link #handlePermissions(String...)} to avoid fiddling with the list and to make it more clear.
     *
     * @param permission the permission
     * @return the result
     */
    public Task<PermissionResponse> handlePermission(String permission) {
        SettableTask<PermissionResponse> response = SettableTask.create(mScope, "handlePermission-" + permission);
        handlePermissions(new String[]{permission}).whenDone(res -> response.set(res.get(0)));
        return response;
    }

    /**
     * Checks if all contained permission responses are granted.
     *
     * @param responses
     * @return true if all permissions have been granted.
     */
    public static boolean allGranted(List<PermissionResponse> responses) {
        int grants = 0;
        for (PermissionResponse r : responses) {
            if (r.isGranted()) {
                grants++;
            }
        }
        return responses.size() == grants;
    }


    /**
     * A specialized response type for permissions. There are so many, that a custom object to wrap the strings does not make sense.
     */
    public final static class PermissionResponse {
        private final String permission;
        private final int result;

        PermissionResponse(String permission, int result) {
            this.permission = permission;
            this.result = result;
        }

        /**
         * The permission, one of {@link permission}
         *
         * @return the permission
         */
        public String getPermission() {
            return permission;
        }

        /**
         * Checks the granted flag
         *
         * @return true if granted
         */
        public boolean isGranted() {
            return result == PackageManager.PERMISSION_GRANTED;
        }

        /**
         * Checks the denied flag
         *
         * @return true if denied
         */
        public boolean isDenied() {
            return result == PackageManager.PERMISSION_DENIED;
        }

        /**
         * Returns the result value as invoked by {@link ActivityCompat#requestPermissions(Activity, String[], int)}
         * and defined in {@link PackageManager}
         *
         * @return the integer result code
         */
        public int getResult() {
            return result;
        }

        /**
         * Wraps this type into a result for your convenience. If denied, the value is null, however additional
         * information is in the tags.
         *
         * @return a new result instance
         */
        public Result<String> asResult() {
            if (isGranted()) {
                return Result.create(permission);
            } else {
                Result<String> res = Result.create();
                res.put("permission.denied");
                res.put("permission", permission);
                res.put("flag", result);
                return res;
            }
        }
    }


    public Activity getActivity() {
        return mEventDispatcher.getActivity();
    }

    @Override
    public void destroy() {
        mEventDispatcher.unregister(mCallback);
    }

    private class RequestHolder {
        final String[] requestedPermissions;
        final SettableTask<List<PermissionResponse>> task;
        final int id;
        final PermissionResponse[] responses;

        RequestHolder(Scope scope, String... permissions) {
            this.id = ActivityEventDispatcher.generateNextRequestId();
            this.requestedPermissions = permissions;
            this.task = SettableTask.create(scope, "handlePermissions-" + Arrays.toString(permissions));
            this.responses = new PermissionResponse[permissions.length];
        }

        void updateResponse(String permission, int grant) {
            for (int i = 0; i < responses.length; i++) {
                if (responses[i] != null && responses[i].permission.equals(permission)) {
                    responses[i] = new PermissionResponse(permission, grant);
                }
            }
        }

    }
}
