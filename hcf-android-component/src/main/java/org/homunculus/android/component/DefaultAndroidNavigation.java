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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.SwitchAnimationLayout.AnimationProvider;
import org.homunculus.android.component.SwitchAnimationLayout.DefaultAnimation;
import org.homunculusframework.factory.container.Request;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.navigation.BackActionConsumer;
import org.homunculusframework.navigation.DefaultNavigation;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;

import javax.annotation.Nullable;

/**
 * A default implementation of {@link NavigationBuilder} with Android flavor. {@link #backward()}
 * has a special behavior, in which it asks the content view of a potential {@link EventAppCompatActivity}
 * for a {@link BackActionConsumer#backward()} action before delegating to {@link DefaultNavigation#backward()}
 * which in turn asks the {@link UserInterfaceState#getBean()} the same before popping the actual stack.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class DefaultAndroidNavigation extends DefaultNavigation implements NavigationBuilder {

    @Nullable
    private NavigationBlockingDialog blockingIndicatorDialog;

    public DefaultAndroidNavigation(Scope scope) {
        super(scope);
    }


    @Override
    public void forward(Request request) {
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
    public void backward(Request request) {
        super.backward(request);
    }


    @Override
    protected void onBeforeApply(@Nullable Request currentRequest, Request nextRequest) {
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
    protected void onAfterApply(@Nullable Request currentRequest, Request nextRequest, @Nullable Throwable details) {
        super.onAfterApply(currentRequest, nextRequest, details);

        NavigationBlockingDialog dlg = blockingIndicatorDialog;
        if (dlg != null) {
            dlg.cancel();
            blockingIndicatorDialog = null;
        }
    }

    @Override
    public AndroidNavigation forward(Class<?> type) {
        return new AndroidNavigation(this, Stack.FORWARD, type);
    }

    @Override
    public AndroidNavigation forward(String beanName) {
        return new AndroidNavigation(this, Stack.FORWARD, beanName);
    }

    @Override
    public AndroidNavigation backward(Class<?> type) {
        return new AndroidNavigation(this, Stack.BACKWARD, type);
    }

    @Override
    public AndroidNavigation backward(String beanName) {
        return new AndroidNavigation(this, Stack.BACKWARD, beanName);
    }

    @Override
    public AndroidNavigation redirect(Class<?> type) {
        return new AndroidNavigation(this, Stack.REDIRECT, type);
    }

    @Override
    public AndroidNavigation redirect(String beanName) {
        return new AndroidNavigation(this, Stack.REDIRECT, beanName);
    }

    public static class AndroidNavigation {

        private final Navigation navigation;
        private final Request request;
        private final Stack direction;

        public AndroidNavigation(Navigation navigation, Stack direction, Class<?> type) {
            this.request = new Request(type);
            this.navigation = navigation;
            this.direction = direction;
        }

        public AndroidNavigation(Navigation navigation, Stack direction, String mapping) {
            this.request = new Request(mapping);
            this.navigation = navigation;
            this.direction = direction;
        }

        public AndroidNavigation put(String key, Object value) {
            request.put(key, value);
            return this;
        }


        public AndroidAnimation animate() {
            return new AndroidAnimation(this);
        }

        public void start() {
            animate().nothing().start();
        }
    }


    public static class AndroidAnimation {
        private final AndroidNavigation navigation;
        private AnimationProvider animation;


        public AndroidAnimation(AndroidNavigation navigation) {
            this.navigation = navigation;
        }

        public AndroidNavigationFinish setCustomAnimation(AnimationProvider animationProvider) {
            animation = animationProvider;
            return finish();
        }

        /**
         * Slides old and new view from right to left, which is usually the forward animation
         *
         * @return the finisher
         */
        public AndroidNavigationFinish slideLeft() {
            animation = DefaultAnimation.SLIDE_LEFT;
            return finish();
        }

        /**
         * Slides old and new view from left to right, which is usually the backward animation
         *
         * @return the finisher
         */
        public AndroidNavigationFinish slideRight() {
            animation = DefaultAnimation.SLIDE_RIGHT;
            return finish();
        }

        /**
         * performs no animation
         *
         * @return the finisher
         */
        public AndroidNavigationFinish nothing() {
            animation = DefaultAnimation.REPLACE;
            return finish();
        }

        private AndroidNavigationFinish finish() {
            return new AndroidNavigationFinish(navigation, this);
        }
    }

    public static class AndroidNavigationFinish {
        private final AndroidNavigation navigation;

        public AndroidNavigationFinish(AndroidNavigation navigation, AndroidAnimation animation) {
            this.navigation = navigation;
        }

        /**
         * Starts the navigation
         */
        public void start() {
            switch (navigation.direction) {
                case BACKWARD:
                    navigation.navigation.backward(navigation.request);
                    break;
                case FORWARD:
                    navigation.navigation.forward(navigation.request);
                    break;
                case REDIRECT:
                    navigation.navigation.redirect(navigation.request);
                    break;
                default:
                    throw new Panic();
            }
        }
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
