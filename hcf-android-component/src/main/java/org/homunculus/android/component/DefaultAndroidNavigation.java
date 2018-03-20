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

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.module.uncaughtexception.UncaughtException.BindUncaughtException;
import org.homunculusframework.factory.container.Binding;
import org.homunculusframework.navigation.BackActionConsumer;
import org.homunculusframework.navigation.DefaultNavigation;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * A default implementation of {@link Navigation} with Android flavor. {@link #backward()}
 * has a special behavior, in which it asks the content view of a potential {@link EventAppCompatActivity}
 * for a {@link BackActionConsumer#backward()} action before delegating to {@link DefaultNavigation#backward()}
 * which in turn asks the {@link UserInterfaceState#getBean()} the same before popping the actual stack.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class DefaultAndroidNavigation extends DefaultNavigation implements Navigation {

    @Nullable
    private NavigationBlockingDialog blockingIndicatorDialog;

    public DefaultAndroidNavigation(Scope scope) {
        super(scope);
    }


    @Override
    public void forward(Binding<?, ?> request) {
        super.forward(request);
    }

    @Override
    public boolean backward() {
        EventAppCompatActivity activity = getScope().resolve(EventAppCompatActivity.class);
        if (activity != null && activity.getContentView() instanceof BackActionConsumer) {
            if (((BackActionConsumer) activity.getContentView()).backward()) {
                return true;
            }
        }
        return super.backward();
    }

    @Override
    public void backward(Binding< ?, ?> request) {
        super.backward(request);
    }


    @Override
    protected void onBeforeApply(@Nullable Binding< ?, ?> currentRequest, Binding< ?, ?> nextRequest) {
        super.onBeforeApply(currentRequest, nextRequest);

        NavigationBlockingDialog dlg = blockingIndicatorDialog;
        if (dlg != null) {
            dlg.cancel();
            blockingIndicatorDialog = null;
        }
        dlg = new NavigationBlockingDialog(getScope().resolve(Context.class));
        dlg.show();
        blockingIndicatorDialog = dlg;

    }

    @Override
    protected void onAfterApply(@Nullable Binding< ?, ?> currentRequest, Binding< ?, ?> nextRequest, @Nullable Throwable details) {
        super.onAfterApply(currentRequest, nextRequest, details);

        NavigationBlockingDialog dlg = blockingIndicatorDialog;
        if (dlg != null) {
            dlg.cancel();
            blockingIndicatorDialog = null;
        }
        if (details != null) {
            redirect(onError(details, currentRequest, nextRequest));
        }
    }

    /**
     * Called to redirect after applying and an error occured.
     *
     * @param details
     * @param currentRequest
     * @param nextRequest
     * @return
     */
    protected Binding< ?, ?> onError(Throwable details, @Nullable Binding< ?, ?> currentRequest, Binding< ?, ?> nextRequest) {
        details.printStackTrace();
        return new BindUncaughtException(details, null);
    }


    enum Stack {
        FORWARD,
        BACKWARD,
        REDIRECT
    }

    public static class NavigationBlockingDialog extends Dialog {

        public NavigationBlockingDialog(@NonNull Context context) {
            super(context);
            onCreate();
        }

        protected void onCreate() {
            getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            setCancelable(false);
            FrameLayout layout = new FrameLayout(getContext());
            layout.setBackgroundColor(Color.TRANSPARENT);
            ProgressBar pg = new ProgressBar(getContext());
            pg.setIndeterminate(true);
            Display dsp = Display.from(getContext());
            layout.addView(pg, new LayoutParams(dsp.dipToPix(64), dsp.dipToPix(64), Gravity.CENTER));
            setContentView(layout);
        }
    }
}
