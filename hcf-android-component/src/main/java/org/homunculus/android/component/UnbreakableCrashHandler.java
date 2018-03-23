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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.FrameLayout;

import org.homunculus.android.core.AndroidScopeContext;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.lang.Function;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;

import javax.annotation.Nullable;

/**
 * This crash handler tries to un-break your android handler by reviving the looper (and potentionally applying a little hack).
 * In swing or JavaFX (or a server) there is never a problem with any uncaught exception, however Android dies a miserable death.
 * The main reason is, that the main looper jumps out and does not process any messages anymore. So we take care of calling
 * the looper ourself. You should install this only once, at application time. However there are situations when this does not work gracefully:
 * <ul>
 * <li>A view failure (e.g. layouting) without resetting the view, will cause an infinite loop. So always reset your entire view or restart your activity</li>
 * <li>You don't know which Activity caused the bug, so you have to reset all of them. You can use {@link Scope#forEachScope(Function)} to find them</li>
 * <li>Some view bugs disturb the entire rendering of the activity, so we need some trickery to reset it (e.g. requesting a permission)</li>
 * </ul>
 * <p>
 * Hint: Provide a {@link AndroidScopeContext} and implement in your activity {@link UncaughtExceptionHandler} and get invoked automagically.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class UnbreakableCrashHandler {

    private final Handler mMainHandler;
    private Context mAppContext;

    public UnbreakableCrashHandler() {
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Install your delegated exception handler. You will be called by all threads, which have been crashed.
     * If this was the main looper, you will be posted. So usually you want to inspect your scopes and
     * send the user to some info UIS. See also {@link UnbreakableCrashHandler}
     *
     * @param handler the crashing callback, always invoked from the main thread (posted at any time in the future after the crash)
     */
    public void install(Context context, @Nullable UncaughtExceptionHandler handler) {
        mAppContext = context;
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                print(e);
                resurrect(t, e, handler);
            }
        });
    }

    /**
     * See {@link #install(Context, UncaughtExceptionHandler)}
     *
     * @param context the context
     */
    public void install(Context context) {
        install(context, null);
    }

    protected void print(Throwable e) {
        LoggerFactory.getLogger(getClass()).error("caught uncaught exception", e);
    }

    protected void resurrect(Thread t, Throwable e, @Nullable UncaughtExceptionHandler handler) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            //handle the first crash of the main looper
            fixRenderingAndDispatchCrash(t, e, handler);
            while (true) {
                try {
                    Looper.loop();
                    throw new RuntimeException("Main thread loop unexpectedly exited");
                } catch (Throwable followUpExceptions) {
                    //at this point, we never enter the "uncaught" exception again, because our looper is already protected
                    print(e);
                    fixRenderingAndDispatchCrash(t, e, handler);
                }
            }
        } else {
            //not the main thread, nothing to revive, but for the sake of consistency we treat it the same
            fixRenderingAndDispatchCrash(t, e, handler);
        }
    }

    protected void fixRenderingAndDispatchCrash(Thread t, Throwable e, @Nullable UncaughtExceptionHandler handler) {
        Thread otherThread = new Thread() {
            @Override
            public void run() {
                mMainHandler.post(() -> {
                    //invoke the given handler
                    if (handler != null) {
                        handler.uncaughtException(t, e);
                    }


                    //dispatch, when possible
                    Scope appScope = AndroidScopeContext.getScope(mAppContext);
                    if (appScope != null) {
                        recursiveFixUI(appScope);
                        recursiveDispatch(appScope, t, e);
                    }
                });
            }
        };
        otherThread.setName("pacemaker");
        otherThread.start();
    }

    /**
     * walks recursive up (not down) the scope from the root and tries to repair all activities.
     */
    protected void recursiveFixUI(Scope root) {
        root.forEachEntry(entry -> {
            if (entry instanceof Activity) {
                Activity activity = ((Activity) entry);
                //clear any broken view
                FrameLayout tmp = new FrameLayout(activity);
                activity.setContentView(tmp);
                LoggerFactory.getLogger(getClass()).warn("reset content view with empty view");

                //fix around weired redraw/buffer/vsync problem, see also https://source.android.com/devices/graphics/implement-vsync

                //now it get awkward: we need to resync some lost events for drawing: force a onPause and onResume cycle to fix it
                launch(activity);
                //the following delay is required for exceptions which occur while failing in onCreate (restoreOnSaveInstance), probably also for immediate errors, does not work "alone"
                mMainHandler.postDelayed(() -> launch(activity), 100);

            }
            return true;
        });
        root.forEachScope(scope -> {
            recursiveFixUI(scope);
            return true;
        });
    }

    protected void launch(Activity activity) {
        try {
            activity.startActivity(new Intent(activity, RecoverActivity.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void recursiveDispatch(Scope root, Thread t, Throwable e) {
        root.forEachEntry(entry -> {
            if (entry instanceof UncaughtExceptionHandler) {
                ((UncaughtExceptionHandler) entry).uncaughtException(t, e);
            }
            return true;
        });
        root.forEachScope(scope -> {
            recursiveDispatch(scope, t, e);
            return true;
        });
    }


    public static class RecoverActivity extends Activity {
        @Override
        protected void onCreate(@android.support.annotation.Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().getDecorView().setBackgroundResource(android.R.color.transparent);
            finish();
        }
    }
}
