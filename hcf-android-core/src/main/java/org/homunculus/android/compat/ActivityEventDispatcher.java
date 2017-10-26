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
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;

import org.homunculusframework.scope.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Used together with {@link EventAppCompatActivity} to provide life cycle callbacks to any one who needs it.
 * Internally the base scope (given by constructor) and all it's children scopes are searched and notified (if they
 * had ever a registration). Scopes are optional at all.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class ActivityEventDispatcher<T extends Activity> {
    private final static String NAME_CALLBACKS = "$activityCallbacks";
    private final static AtomicInteger mRequestCode = new AtomicInteger();
    private final InternalEventDispatcher mDispatcher;
    private final T mActivity;
    private ActivityStatus mStatus;
    private Bundle mSavedInstanceStateAtOnCreate;
    private final Scope mBaseScope;

    public ActivityEventDispatcher(Scope baseScope, T activity) {
        mStatus = ActivityStatus.Launching;
        mActivity = activity;
        mDispatcher = new InternalEventDispatcher();

        mBaseScope = baseScope;
    }


    /**
     * Brute forces the termination of the current process. There is no guarantee that finalizers or {@link Runtime#addShutdownHook(Thread)}
     * are processed.
     */
    public void finishApplication() {
        System.runFinalizersOnExit(true);
        try {
            Process.killProcess(Process.myPid());
        } finally {
            System.exit(0);
        }
    }


    /**
     * Use this method to generate intent or activity or any result/request codes for android badly designed event handling mechanism.
     *
     * @return
     */
    public static int generateNextRequestId() {
        return mRequestCode.incrementAndGet();
    }


    /**
     * Returns the current status in which the current activity is.
     */
    public ActivityStatus getStatus() {
        return mStatus;
    }

    public T getActivity() {
        return mActivity;
    }

    /**
     * Registers a new callback with all activity events in the current top scope. Buffered events are directly called, when necessary. Use only from the main thread, otherwise
     * the result is not defined (e.g. multiple or lost buffered events).
     *
     * @param callback
     */
    public void register(ActivityEventCallback<T> callback) {
        register(mActivity, callback);
    }

    /**
     * Registers a new callback with all activity events within the given scope. See also {@link #register(ActivityEventCallback)}
     * Tries to grab the scope from the given context, and calls {@link #register(Scope, ActivityEventCallback)}
     */
    public void register(@Nullable Context context, ActivityEventCallback<T> callback) {
        Scope scope = ContextScope.getScope(context);
        register(scope, callback);
    }

    /**
     * Registers a new callback with all activity events within the given scope. See also {@link #register(Context, ActivityEventCallback)}
     */
    public void register(@Nullable Scope scope, ActivityEventCallback<T> callback) {
        if (scope == null) {
            scope = mBaseScope;
        }
        switch (mStatus) {
            case Running:
                callback.onBufferedCreate(mActivity, mSavedInstanceStateAtOnCreate);
                callback.onBufferedResume(mActivity);
                break;
            case Pausing:
            case Stopping:
                callback.onBufferedCreate(mActivity, mSavedInstanceStateAtOnCreate);
                //TODO or just do nothing? what will cause more errors?
                callback.onBufferedResume(mActivity);
                callback.onBufferedPause(mActivity);
                break;
            case Destroying:
            case Dead:
                callback.onBufferedCreate(mActivity, mSavedInstanceStateAtOnCreate);
                //TODO or just do nothing? what will cause more errors?
                callback.onBufferedResume(mActivity);
                callback.onBufferedPause(mActivity);
                callback.onBufferedDestroy(mActivity);
                break;

        }
        ensure(scope).add(callback);
    }

    /**
     * We only have a callback list once per scope
     *
     * @param scope
     * @return
     */
    private List<ActivityEventCallback<T>> ensure(Scope scope) {
        synchronized (scope) {
            List<ActivityEventCallback<T>> callbacks = scope.get(NAME_CALLBACKS, List.class);
            if (callbacks == null) {
                callbacks = new CopyOnWriteArrayList<>();
                scope.put(NAME_CALLBACKS, callbacks);
            }
            return callbacks;
        }
    }

    public boolean unregister(ActivityEventCallback<T> callback) {
        return unregister(null, callback);
    }

    public boolean unregister(@Nullable Scope scope, ActivityEventCallback<T> callback) {
        if (scope == null) {
            scope = mBaseScope;
        }
        List<ActivityEventCallback<T>> callbacks = ensure(scope);
        return callbacks.remove(callback);
    }


    /**
     * Returns the internal service dispatcher which is used by WDYActivity to signal all available events
     *
     * @return
     */
    public ActivityEventCallback<T> getEventDispatcher() {
        return mDispatcher;
    }

    public void destroy() {
        getEventDispatcher().onActivityDestroy(mActivity);
    }

    /**
     * The heart of the scope logic here: collect all callbacks from the base scope and of all contained children.
     * Listeners are automatically removed from subtrees as they are destroyed or detached.
     */
    private List<ActivityEventCallback<T>> getCallbacks() {
        List<ActivityEventCallback<T>> tmp = new ArrayList<>();
        List<ActivityEventCallback<T>> listInScope = mBaseScope.get(NAME_CALLBACKS, List.class);
        if (listInScope != null) {
            tmp.addAll(listInScope);
        }
        collectCallbacks(mBaseScope, tmp);
        return tmp;
    }

    private void collectCallbacks(Scope root, List<ActivityEventCallback<T>> dst) {
        root.forEachScope(scope -> {
            List<ActivityEventCallback<T>> listInScope = scope.get(NAME_CALLBACKS, List.class);
            if (listInScope != null) {
                dst.addAll(listInScope);
            }
            //recursive
            collectCallbacks(scope, dst);
            return true;
        });
    }

    //TODO complete implementation
    private class InternalEventDispatcher extends AbsActivityEventCallback<T> {

        @Override
        public void onActionModeStarted(ActionMode mode) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActionModeStarted(mode);
            }
            super.onActionModeStarted(mode);
        }

        @Override
        public void onActionModeFinished(ActionMode mode) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActionModeFinished(mode);
            }
            super.onActionModeFinished(mode);
        }

        @Override
        public void onActivityNewIntent(T activity, Intent intent) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActivityNewIntent(activity, intent);
            }
        }

        @Override
        public void onBufferedPause(T activity) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onBufferedPause(activity);
            }
        }

        @Override
        public void onBufferedResume(T activity) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onBufferedResume(activity);
            }
        }

        @Override
        public void onBufferedCreate(T activity, Bundle savedInstanceState) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onBufferedCreate(activity, savedInstanceState);
            }
        }

        @Override
        public void onBufferedDestroy(T activity) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onBufferedDestroy(activity);
            }
        }

        @Override
        public void onActivityResume(T activity) {
            mStatus = ActivityStatus.Running;
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActivityResume(activity);
            }
            onBufferedResume(activity);
        }

        @Override
        public void onActivityPause(T activity) {
            mStatus = ActivityStatus.Pausing;
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActivityPause(activity);
            }
            onBufferedPause(activity);

        }

        @Override
        public void onActivityStop(T activity) {
            mStatus = ActivityStatus.Stopping;
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActivityStop(activity);
            }
        }

        @Override
        public void onActivityStart(T activity) {
            mStatus = ActivityStatus.Starting;
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActivityStart(activity);
            }
        }

        @Override
        public void onActivityDestroy(T activity) {
            mStatus = ActivityStatus.Destroying;
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActivityDestroy(activity);
            }
            mStatus = ActivityStatus.Dead;
        }

        @Override
        public void onActivityCrash(T activity, Thread thread, Throwable throwable) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActivityCrash(activity, thread, throwable);
            }
        }

        @Override
        public void onLowMemory(T activity) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onLowMemory(activity);
            }
        }

        @Override
        public boolean onActivityBackPressed(T activity) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityBackPressed(activity)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onActivityKeyDown(T activity, int keyCode, KeyEvent event) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityKeyDown(activity, keyCode, event)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onActivityKeyUp(T activity, int keyCode, KeyEvent event) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityKeyUp(activity, keyCode, event)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onActivityCreate(T activity, Bundle savedInstanceState) {
            mSavedInstanceStateAtOnCreate = savedInstanceState;
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActivityCreate(activity, savedInstanceState);
            }
        }

        @Override
        public void onActivityCreate(T activity, Bundle savedInstanceState, PersistableBundle persistentState) {
            mSavedInstanceStateAtOnCreate = savedInstanceState;
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActivityCreate(activity, savedInstanceState, persistentState);
            }
        }

        @Override
        public boolean onActivityKeyLongPress(T activity, int keyCode, KeyEvent event) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityKeyLongPress(activity, keyCode, event)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onActivityKeyMultiple(T activity, int keyCode, int repeatCount, KeyEvent event) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityKeyMultiple(activity, keyCode, repeatCount, event)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onActivityDispatchKeyEvent(T activity, KeyEvent event) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityDispatchKeyEvent(activity, event)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onActivityResult(T activity, int requestCode, int resultCode, Intent data) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityResult(activity, requestCode, resultCode, data)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onActivityCreateOptionsMenu(T activity, Menu menu) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityCreateOptionsMenu(activity, menu)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onActivityMenuOpened(T activity, int featureId, Menu menu) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityMenuOpened(activity, featureId, menu)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onActivityOptionsItemSelected(T activity, MenuItem item) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityOptionsItemSelected(activity, item)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onActivityRequestPermissionsResult(T activity, int requestCode, String[] permissions, int[] grantResults) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                cb.onActivityRequestPermissionsResult(activity, requestCode, permissions, grantResults);
            }
        }

        @Override
        public boolean onActivityPrepareOptionsMenu(T activity, Menu menu) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityPrepareOptionsMenu(activity, menu)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onActivityMenuItemSelected(T activity, int featureId, MenuItem item) {
            for (ActivityEventCallback<T> cb : getCallbacks()) {
                if (cb.onActivityMenuItemSelected(activity, featureId, item)) {
                    return true;
                }
            }
            return false;
        }
    }

    //TODO extend events to be complete with Activities
    public interface ActivityEventCallback<T extends Activity> {
        /**
         * May be called after registering, even if the activity has been resumed already before registering.
         *
         * @param activity
         */
        void onBufferedResume(T activity);

        /**
         * May be called after registering, even if the activity has been resumed already after registering. {@link #onBufferedResume(Activity)}
         * will always be called before.
         *
         * @param activity
         */
        void onBufferedPause(T activity);

        /**
         * May be called after registering, even if the activity has been created already
         *
         * @param activity
         * @param savedInstanceState
         */
        void onBufferedCreate(T activity, Bundle savedInstanceState);

        /**
         * May be called after registering, even if the activity has been created already destroyed
         *
         * @param activity
         */
        void onBufferedDestroy(T activity);


        boolean onActivityGenericMotionEvent(T activity, MotionEvent event);

        void onActivityCrash(T activity, Thread thread, Throwable throwable);

        boolean onActivityCreateThumbnail(T activity, Bitmap outBitmap, Canvas canvas);

        void onActivityCreate(T activity, Bundle savedInstanceState);

        void onActivityCreate(T activity, Bundle savedInstanceState, PersistableBundle persistentState);

        void onActivityPostCreate(T activity, Bundle savedInstanceState);

        void onActivityTitleChanged(T activity, CharSequence title, int color);

        void onActivityUserLeaveHint(T activity);

        ActionMode onActivityWindowStartingActionMode(T activity, Callback callback);

        ActionMode onActivityWindowStartingActionMode(T activity, Callback callback, int type);

        boolean onActivityDispatchKeyEvent(T activity, KeyEvent event);

        void onActivitySaveInstanceState(T activity, Bundle outState);

        void onActivityRestoreInstanceState(T activity, Bundle savedInstanceState);

        void onActivityStart(T activity);

        void onActivityRestart(T activity);

        void onActivityResume(T activity);

        void onActivityPostResume(T activity);

        void onActivityConfigurationChanged(T activity, Configuration newConfig);

        void onActivityPause(T activity);

        void onActivityStop(T activity);

        void onActivityDestroy(T activity);

        void onActionModeStarted(ActionMode mode);

        void onActionModeFinished(ActionMode mode);

        boolean onActivityKeyUp(T activity, int keyCode, KeyEvent event);

        boolean onActivityKeyDown(T activity, int keyCode, KeyEvent event);

        /**
         * See {@link Activity#onActivityResult(int, int, Intent)}
         */
        boolean onActivityResult(T activity, int requestCode, int resultCode, Intent data);

        boolean onActivityCreatePanelMenu(T activity, int featureId, Menu menu);

        boolean onActivityPreparePanel(T activity, int featureId, View view, Menu menu);

        boolean onActivityMenuItemSelected(T activity, int featureId, MenuItem item);

        boolean onActivityMenuOpened(T activity, int featureId, Menu menu);

        boolean onActivityPanelClosed(T activity, int featureId, Menu menu);

        void onActivityRequestPermissionsResult(T activity, int requestCode, String[] permissions, int[] grantResults);

        boolean onActivityContextItemSelected(T activity, MenuItem item);

        boolean onActivityPrepareOptionsMenu(T activity, Menu menu);

        boolean onActivityCreateOptionsMenu(T activity, Menu menu);

        void onActivityChildTitleChanged(T activity, Activity childActivity, CharSequence title);

        void onActivityNewIntent(T activity, Intent intent);

        boolean onActivityKeyLongPress(T activity, int keyCode, KeyEvent event);

        boolean onActivityKeyMultiple(T activity, int keyCode, int repeatCount, KeyEvent event);

        boolean onActivityKeyShortcut(T activity, int keyCode, KeyEvent event);

        boolean onActivityNavigateUp(T activity);

        boolean onActivityNavigateUpFromChild(T activity, Activity child);

        boolean onActivityOptionsItemSelected(T activity, MenuItem item);


        boolean onActivitySearchRequested(T activity);

        boolean onActivitySearchRequested(T activity, SearchEvent searchEvent);

        boolean onActivityTouchEvent(T activity, MotionEvent event);

        boolean onActivityTrackballEvent(T activity, MotionEvent event);

        CharSequence onActivityCreateDescription(T activity);

        Uri onActivityProvideReferrer(T activity);

        View onActivityCreatePanelView(T activity, int featureId);


        boolean onActivityBackPressed(T activity);

        void onLowMemory(T activity);
    }

    public static class AbsActivityEventCallback<T extends Activity> implements ActivityEventCallback<T> {

        @Override
        public void onBufferedPause(T activity) {

        }

        @Override
        public void onBufferedResume(T activity) {

        }

        @Override
        public void onBufferedCreate(T activity, Bundle savedInstanceState) {

        }

        @Override
        public void onBufferedDestroy(T activity) {

        }

        @Override
        public void onActivityCrash(T activity, Thread thread, Throwable throwable) {

        }

        @Override
        public void onActivityRequestPermissionsResult(T activity, int requestCode, String[] permissions, int[] grantResults) {

        }

        @Override
        public boolean onActivityBackPressed(T activity) {
            return false;
        }

        @Override
        public boolean onActivityNavigateUp(T activity) {
            return false;
        }

        @Override
        public boolean onActivityNavigateUpFromChild(T activity, Activity child) {
            return false;
        }

        @Override
        public boolean onActivityOptionsItemSelected(T activity, MenuItem item) {
            return false;
        }

        @Override
        public boolean onActivitySearchRequested(T activity) {
            return false;
        }

        @Override
        public boolean onActivitySearchRequested(T activity, SearchEvent searchEvent) {
            return false;
        }

        @Override
        public boolean onActivityTouchEvent(T activity, MotionEvent event) {
            return false;
        }

        @Override
        public boolean onActivityTrackballEvent(T activity, MotionEvent event) {
            return false;
        }

        @Override
        public CharSequence onActivityCreateDescription(T activity) {
            return null;
        }

        @Override
        public Uri onActivityProvideReferrer(T activity) {
            return null;
        }

        @Override
        public View onActivityCreatePanelView(T activity, int featureId) {
            return null;
        }

        @Override
        public void onActionModeStarted(ActionMode mode) {
        }

        @Override
        public void onActionModeFinished(ActionMode mode) {
        }

        @Override
        public void onLowMemory(T activity) {

        }

        @Override
        public boolean onActivityKeyShortcut(T activity, int keyCode, KeyEvent event) {
            return false;
        }

        @Override
        public boolean onActivityKeyMultiple(T activity, int keyCode, int repeatCount, KeyEvent event) {
            return false;
        }

        @Override
        public boolean onActivityKeyLongPress(T activity, int keyCode, KeyEvent event) {
            return false;
        }

        @Override
        public boolean onActivityGenericMotionEvent(T activity, MotionEvent event) {
            return false;
        }

        @Override
        public boolean onActivityCreateThumbnail(T activity, Bitmap outBitmap, Canvas canvas) {
            return false;
        }

        @Override
        public ActionMode onActivityWindowStartingActionMode(T activity, Callback callback, int type) {
            return null;
        }

        @Override
        public ActionMode onActivityWindowStartingActionMode(T activity, Callback callback) {
            return null;
        }

        @Override
        public void onActivityUserLeaveHint(T activity) {

        }

        @Override
        public void onActivityNewIntent(T activity, Intent intent) {

        }


        @Override
        public void onActivityCreate(T activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityCreate(T activity, Bundle savedInstanceState, PersistableBundle persistentState) {

        }

        @Override
        public void onActivityPostCreate(T activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityTitleChanged(T activity, CharSequence title, int color) {

        }

        @Override
        public boolean onActivityDispatchKeyEvent(T activity, KeyEvent event) {
            return false;
        }

        @Override
        public void onActivitySaveInstanceState(T activity, Bundle outState) {

        }

        @Override
        public void onActivityRestoreInstanceState(T activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStart(T activity) {

        }

        @Override
        public void onActivityRestart(T activity) {

        }

        @Override
        public void onActivityResume(T activity) {

        }

        @Override
        public void onActivityPostResume(T activity) {

        }

        @Override
        public void onActivityConfigurationChanged(T activity, Configuration newConfig) {

        }

        @Override
        public void onActivityPause(T activity) {

        }

        @Override
        public void onActivityStop(T activity) {

        }

        @Override
        public void onActivityDestroy(T activity) {

        }

        @Override
        public boolean onActivityKeyUp(T activity, int keyCode, KeyEvent event) {
            return false;
        }

        @Override
        public boolean onActivityKeyDown(T activity, int keyCode, KeyEvent event) {
            return false;
        }

        @Override
        public boolean onActivityResult(T activity, int requestCode, int resultCode, Intent data) {
            return false;
        }

        @Override
        public boolean onActivityCreatePanelMenu(T activity, int featureId, Menu menu) {
            return false;
        }

        @Override
        public boolean onActivityPreparePanel(T activity, int featureId, View view, Menu menu) {
            return false;
        }

        @Override
        public boolean onActivityMenuItemSelected(T activity, int featureId, MenuItem item) {
            return false;
        }

        @Override
        public boolean onActivityMenuOpened(T activity, int featureId, Menu menu) {
            return false;
        }

        @Override
        public boolean onActivityPanelClosed(T activity, int featureId, Menu menu) {
            return false;
        }

        @Override
        public boolean onActivityContextItemSelected(T activity, MenuItem item) {
            return false;
        }

        @Override
        public boolean onActivityPrepareOptionsMenu(T activity, Menu menu) {
            return false;
        }

        @Override
        public boolean onActivityCreateOptionsMenu(T activity, Menu menu) {
            return false;
        }


        @Override
        public void onActivityChildTitleChanged(T activity, Activity childActivity, CharSequence title) {

        }
    }

    /**
     * See the following diagram to remember the lifecycle:
     * <p>
     * <img src="http://developer.android.com/images/activity_lifecycle.png"/>
     */
    public enum ActivityStatus {
        Launching,
        Creating,
        Starting,
        Running,
        Pausing,
        Stopping,
        Destroying,
        Dead
    }

}
