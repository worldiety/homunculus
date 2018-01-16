package org.homunculus.android.component;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;

import org.homunculus.android.flavor.AndroidMainHandler;
import org.homunculusframework.concurrent.Task;
import org.homunculusframework.lang.Function;
import org.homunculusframework.lang.Panic;

import javax.annotation.Nullable;

/**
 * A utility class to provide some debounced helper methods for some common use cases.
 * <p>
 * Created by Torben Schinke on 16.01.18.
 */
public class Debounce {

    private Debounce() {
        throw new Panic();
    }

    /**
     * Debounces the given view and the given click listener in a way that the instance of the view can only get
     * clicked once, until either {@link #clearClick(View)} and {@link #clickOnce(View, OnClickListener)} is
     * called once more or if a new instance of the view is created.
     *
     * @param where the view to attach a single action. After clicking the view is disabled "forever"
     * @param what  the action to execute exactly once
     */
    public static void clickOnce(View where, @Nullable OnClickListener what) {
        Object tmp = where.getTag(R.id.debounce_view_adapter);
        if (!(tmp instanceof DebounceAdapterOnce)) {
            DebounceAdapterOnce adapter = new DebounceAdapterOnce(where, what);
            where.setTag(R.id.debounce_view_adapter, adapter);
            where.setOnClickListener(adapter);
            where.setEnabled(true);
        }
    }

    /**
     * Debounces the given view so that it is disabled while the returned task is not done yet.
     *
     * @param where       the view to attach
     * @param taskFactory the factory function to create the task when clicked
     */
    public static void click(View where, Function<View, Task<?>> taskFactory) {
        Object tmp = where.getTag(R.id.debounce_view_adapter);
        if (!(tmp instanceof DebounceAdapter)) {
            DebounceAdapter adapter = new DebounceAdapter(where, taskFactory);
            where.setTag(R.id.debounce_view_adapter, adapter);
            where.setOnClickListener(adapter);
        }
    }

    /**
     * Removes any registered click listener
     *
     * @param where the view to clear
     */
    public static void clearClick(View where) {
        where.setTag(R.id.debounce_view_adapter, null);
        where.setOnClickListener(null);
        where.setEnabled(true);
    }


    private static class DebounceAdapterOnce implements OnClickListener {
        private final OnClickListener delegate;
        private boolean hasRun;
        private final View view;

        private DebounceAdapterOnce(View view, @Nullable OnClickListener delegate) {
            this.delegate = delegate;
            this.view = view;
        }

        @Override
        public void onClick(View v) {
            if (delegate != null && !hasRun) {
                view.setEnabled(false);
                hasRun = true;
                delegate.onClick(v);
            }
        }
    }

    private static class DebounceAdapter implements OnClickListener {
        private final Function<View, Task<?>> taskFactory;
        private final View view;
        private final Handler handler;

        public DebounceAdapter(View view, Function<View, Task<?>> taskFactory) {
            this.taskFactory = taskFactory;
            this.view = view;
            this.handler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onClick(View v) {
            Task<?> task = taskFactory.apply(v);
            view.setEnabled(false);
            task.whenDone(res -> {
                if (AndroidMainHandler.isMainThread()) {
                    view.setEnabled(true);
                } else {
                    handler.post(() -> {
                        view.setEnabled(true);
                    });
                }

            });
        }
    }
}
