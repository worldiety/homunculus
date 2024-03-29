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
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.homunculus.android.core.ActivityEventDispatcher;
import org.homunculus.android.core.ActivityEventOwner;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.navigation.Navigation;
import org.slf4j.LoggerFactory;

/**
 * An activity which helps to write lifecycle and scope aware components. This is long awaited wish on the Android Activity
 * which has not been fulfilled yet. However Google starts to think into the same direction
 * and at least provides the architecture API, see also https://developer.android.com/topic/libraries/architecture/lifecycle.html.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class EventAppCompatActivity extends AppCompatActivity implements ActivityEventOwner {
    private ActivityEventDispatcher<EventAppCompatActivity> mEventDispatcher;
    private boolean mEverCreated;
    private View mContentView;


    /**
     * Brute forces the termination of the current process. There is no guarantee that finalizers or {@link Runtime#addShutdownHook(Thread)}
     * are processed.
     */
    public void finishApplication() {
        System.runFinalization();
        //System.runFinalizersOnExit(true);
        try {
            Process.killProcess(Process.myPid());
        } finally {
            System.exit(0);
        }
    }


    /**
     * Returns the scope of this activity.
     */
    public Scope getScope() {
        return null;
    }
/*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiDex.install(this);
        init();
        mEventDispatcher.getEventDispatcher().onActivityCreate(this, savedInstanceState);
    }

 */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        mEventDispatcher.getEventDispatcher().onActivityCreate(this, savedInstanceState);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        mContentView = view;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(layoutResID, null, false);
        setContentView(view);
    }


    /**
     * Returns the view which has been set by {@link #setContentView(View)} or {@link #setContentView(int)}
     */
    @Nullable
    public View getContentView() {
        return mContentView;
    }

    private void init() {
        mEventDispatcher = new ActivityEventDispatcher<>(getScope(), this);
        mEverCreated = true;
//        mScope.put(Android.NAME_ACTIVITY_EVENT_DISPATCHER, mEventDispatcher);
    }


    @Override
    public ActivityEventDispatcher<EventAppCompatActivity> getEventDispatcher() {
        return mEventDispatcher;
    }


    @Override
    protected void onPause() {
        mEventDispatcher.getEventDispatcher().onActivityPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEventDispatcher.getEventDispatcher().onActivityResume(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);
        mEventDispatcher.getEventDispatcher().onActivityPostCreate(this, savedInstanceState);
    }
/*
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }


 */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mEventDispatcher.getEventDispatcher().onActivitySaveInstanceState(this, outState);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mEventDispatcher.getEventDispatcher().onActivityPostResume(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mEventDispatcher.getEventDispatcher().onActivityResult(this, requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mEventDispatcher.getEventDispatcher().onActivityStart(this);
    }

    @Override
    protected void onStop() {
        mEventDispatcher.getEventDispatcher().onActivityStop(this);
        super.onStop();
    }


    @Override
    protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
        super.onChildTitleChanged(childActivity, title);
        mEventDispatcher.getEventDispatcher().onActivityChildTitleChanged(this, childActivity, title);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mEventDispatcher.getEventDispatcher().onActivityNewIntent(this, intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mEventDispatcher.getEventDispatcher().onActivityRestart(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mEventDispatcher.getEventDispatcher().onActivityRestoreInstanceState(this, savedInstanceState);
    }


    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        mEventDispatcher.getEventDispatcher().onActivityTitleChanged(this, title, color);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        mEventDispatcher.getEventDispatcher().onActivityUserLeaveHint(this);
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(Callback callback) {
        ActionMode mode = mEventDispatcher.getEventDispatcher().onActivityWindowStartingActionMode(this, callback);
        if (mode == null) {
            return super.onWindowStartingActionMode(callback);
        } else {
            return mode;
        }
    }


    @Nullable
    @Override
    public ActionMode onWindowStartingActionMode(Callback callback, int type) {
        ActionMode mode = mEventDispatcher.getEventDispatcher().onActivityWindowStartingActionMode(this, callback, type);
        if (mode == null) {
            return super.onWindowStartingActionMode(callback, type);
        } else {
            return mode;
        }
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        mEventDispatcher.getEventDispatcher().onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        mEventDispatcher.getEventDispatcher().onActionModeFinished(mode);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (mEventDispatcher.getEventDispatcher().onActivityContextItemSelected(this, item)) {
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (invalidLifeState()) {
            return false;
        }
        if (mEventDispatcher.getEventDispatcher().onActivityCreateOptionsMenu(this, menu)) {
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (invalidLifeState()) {
            return false;
        }
        if (mEventDispatcher.getEventDispatcher().onActivityCreatePanelMenu(this, featureId, menu)) {
            return true;
        } else {
            return super.onCreatePanelMenu(featureId, menu);
        }
    }

    /*
    This method was deprecated in API level 28.
    @Override
    public boolean onCreateThumbnail(Bitmap outBitmap, Canvas canvas) {
        if (invalidLifeState()) {
            return false;
        }
        if (mEventDispatcher.getEventDispatcher().onActivityCreateThumbnail(this, outBitmap, canvas)) {
            return true;
        } else {
            return super.onCreateThumbnail(outBitmap, canvas);
        }
    }

     */

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        if (invalidLifeState()) {
            return false;
        }
        if (mEventDispatcher.getEventDispatcher().onActivityGenericMotionEvent(this, event)) {
            return true;
        } else {
            return super.onGenericMotionEvent(event);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mEventDispatcher.getEventDispatcher().onActivityKeyDown(this, keyCode, event)) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (mEventDispatcher.getEventDispatcher().onActivityKeyLongPress(this, keyCode, event)) {
            return true;
        } else {
            return super.onKeyLongPress(keyCode, event);
        }
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        if (mEventDispatcher.getEventDispatcher().onActivityKeyMultiple(this, keyCode, repeatCount, event)) {
            return true;
        } else {
            return super.onKeyMultiple(keyCode, repeatCount, event);
        }
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        if (mEventDispatcher.getEventDispatcher().onActivityKeyShortcut(this, keyCode, event)) {
            return true;
        } else {
            return super.onKeyShortcut(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mEventDispatcher.getEventDispatcher().onActivityKeyUp(this, keyCode, event)) {
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }


    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (mEventDispatcher.getEventDispatcher().onActivityMenuOpened(this, featureId, menu)) {
            return true;
        } else {
            return super.onMenuOpened(featureId, menu);
        }
    }

    @Override
    public boolean onNavigateUp() {
        if (invalidLifeState()) {
            return false;
        }
        if (mEventDispatcher.getEventDispatcher().onNavigateUp()) {
            return true;
        } else {
            return super.onNavigateUp();
        }
    }


/*
    Depracted since API Level 28
    @Override
    public boolean onNavigateUpFromChild(Activity child) {
        if (invalidLifeState()) {
            return false;
        }
        if (mEventDispatcher.getEventDispatcher().onActivityNavigateUpFromChild(this, child)) {
            return true;
        } else {
            return super.onNavigateUp();
        }
    }

 */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (invalidLifeState()) {
            return false;
        }
        if (mEventDispatcher.getEventDispatcher().onActivityOptionsItemSelected(this, item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (invalidLifeState()) {
            return false;
        }
        if (mEventDispatcher.getEventDispatcher().onActivityPrepareOptionsMenu(this, menu)) {
            return true;
        } else {
            return super.onPrepareOptionsMenu(menu);
        }
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (invalidLifeState()) {
            return false;
        }
        if (mEventDispatcher.getEventDispatcher().onActivityPreparePanel(this, featureId, view, menu)) {
            return true;
        } else {
            return super.onPreparePanel(featureId, view, menu);
        }
    }

    @Override
    public boolean onSearchRequested() {
        if (invalidLifeState()) {
            return false;
        }
        if (mEventDispatcher.getEventDispatcher().onActivitySearchRequested(this)) {
            return true;
        } else {
            return super.onSearchRequested();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mEventDispatcher.getEventDispatcher().onActivityTouchEvent(this, event)) {
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean onSearchRequested(SearchEvent searchEvent) {
        if (mEventDispatcher.getEventDispatcher().onActivitySearchRequested(this, searchEvent)) {
            return true;
        } else {
            return super.onSearchRequested(searchEvent);
        }
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (mEventDispatcher.getEventDispatcher().onActivityTrackballEvent(this, event)) {
            return true;
        } else {
            return super.onTrackballEvent(event);
        }
    }

    @Nullable
    @Override
    public CharSequence onCreateDescription() {
        CharSequence res = mEventDispatcher.getEventDispatcher().onActivityCreateDescription(this);
        if (res == null) {
            return super.onCreateDescription();
        } else {
            return res;
        }
    }

    @Override
    public Uri onProvideReferrer() {
        Uri res = mEventDispatcher.getEventDispatcher().onActivityProvideReferrer(this);
        if (res == null) {
            return super.onProvideReferrer();
        } else {
            return res;
        }
    }

    @Nullable
    @Override
    public View onCreatePanelView(int featureId) {
        View res = mEventDispatcher.getEventDispatcher().onActivityCreatePanelView(this, featureId);
        if (res == null) {
            return super.onCreatePanelView(featureId);
        } else {
            return res;
        }
    }


    /**
     * Assumes a {@link Navigation} instance and goes backwards on it.
     *
     * @return true if the navigation is about going backwards, false otherwise (either because the stack is empty or there is no navigation at all)
     */
    protected boolean onDispatchNavigationBackPressed() {
        Navigation navigation = getScope().resolve(Navigation.class);
        if (navigation != null) {
            if (!navigation.backward()) {
                return false;
            } else {
                return true;
            }
        } else {
            LoggerFactory.getLogger(getClass()).error("no navigation available");
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (!mEventDispatcher.getEventDispatcher().onActivityBackPressed(this)) {
            super.onBackPressed();
        }
    }

    @Override
    public void onLowMemory() {
        mEventDispatcher.getEventDispatcher().onLowMemory(this);
        super.onLowMemory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mEventDispatcher.getEventDispatcher().onActivityConfigurationChanged(this, newConfig);
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mEventDispatcher.getEventDispatcher().onActivityDispatchKeyEvent(this, event)) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        if (invalidLifeState()) {
            return;
        }
        if (!mEventDispatcher.getEventDispatcher().onActivityPanelClosed(this, featureId, menu)) {
            super.onPanelClosed(featureId, menu);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mEventDispatcher.getEventDispatcher().onActivityRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }


    @Override
    protected void onDestroy() {
        mEventDispatcher.destroy();
        super.onDestroy();
        mEventDispatcher = null;
        Scope scope = getScope();
        if (scope != null) {
            scope.onDestroy();
        }
    }


    private boolean invalidLifeState() {
        if (mEventDispatcher == null) {
            String msg = mEverCreated ? "after destroy" : "before onCreate";
            LoggerFactory.getLogger(getClass()).error("the activity {} is used in an invalid state: {}", this, msg);
            return true;
        } else {
            return false;
        }
    }
}
