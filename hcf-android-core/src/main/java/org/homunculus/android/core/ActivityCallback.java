package org.homunculus.android.core;

import android.app.Activity;

import androidx.annotation.Nullable;

import org.homunculus.android.core.ActivityEventDispatcher.ActivityEventCallback;
import org.homunculusframework.factory.scope.LifecycleOwner;
import org.homunculusframework.scope.LifecycleLocal;
import org.homunculusframework.scope.OnDestroyCallback;

;

/**
 * Created by Torben Schinke on 20.03.18.
 */

public class ActivityCallback<T extends Activity> extends LifecycleLocal<ActivityEventCallback> {

    private ActivityEventCallback<T> delegate;
    private final ActivityEventDispatcher<T> activityEventDispatcher;
    private final OnDestroyCallback callback;

    public ActivityCallback(LifecycleOwner lifecycle, ActivityEventDispatcher<T> eventDispatcher) {
        super(lifecycle);
        this.activityEventDispatcher = eventDispatcher;
        this.callback = s -> setDelegate(null);
        lifecycle.addDestroyCallback(this.callback);
    }

    public void setDelegate(@Nullable ActivityEventCallback<T> delegate) {
        synchronized (this) {
            if (this.delegate != null) {
                activityEventDispatcher.unregister(this.delegate);
                this.delegate = null;
            }
            this.delegate = delegate;
            if (delegate != null) {
                activityEventDispatcher.register(delegate);
            }
        }
    }

    public Activity getActivity() {
        return activityEventDispatcher.getActivity();
    }
}
