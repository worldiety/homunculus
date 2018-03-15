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
package org.homunculus.android.component.module.uncaughtexception;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import org.homunculus.android.component.MaterialFont.Icon;
import org.homunculus.android.component.MaterialFontView;
import org.homunculus.android.component.R;
import org.homunculus.android.core.Android;
import org.homunculus.android.flavor.Resource;
import org.homunculusframework.factory.container.ObjectBinding;
import org.homunculusframework.lang.Result;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;
import org.homunculusframework.scope.SettableTask;
import org.homunculusframework.stereotype.UserInterfaceState;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * A simple default UIS which provides a standard crash view which is displayed automatically by
 * {@link org.homunculus.android.component.HomunculusActivity}. To lower the risk of an infinite crash loop,
 * it replaces the entire content view of the activity. It provides a send button to submit the information
 * as a report using an optional {@link Reporter}. Also works without a configured reporter.
 * <p>
 * The optional {@link UncaughtException#PARAM_RESULT} or {@link UncaughtException#PARAM_THROWABLE} are passed
 * to the reporter.
 *
 * @author Torben Schinke
 * @since 1.0
 */
@Named(UncaughtException.NAME)
@UserInterfaceState
public class UncaughtException {
    /**
     * The bean name
     */
    public final static String NAME = "hcf/uncaughtexception";

    /**
     * optional parameter name for a {@link Throwable}, key/value passed to the optional {@link Reporter}
     */
    public final static String PARAM_THROWABLE = "throwable";
    /**
     * Optional parameter name for a {@link Result}, key/value passed to the optional {@link Reporter}
     */
    public final static String PARAM_RESULT = "result";


    @Inject
    private Activity activity;

    @Nullable
    private Throwable throwable;

    @Nullable
    private Result<?> result;

    @Nullable
    private Reporter reporter;

    private Navigation navigation;

    private Scope scope;

    private void apply() {
        View v = LayoutInflater.from(activity).inflate(R.layout.hcf_uncaughtexception, null);
        activity.setContentView(v);
        View btnBack = v.findViewById(R.id.hcf_uncaughtexception_button_back);
        if (navigation == null) {
            btnBack.setVisibility(View.GONE);
        } else {
            btnBack.setOnClickListener(view -> {
                if (!navigation.backward()) {
                    activity.finish();
                }
            });
        }

        View btnSend = v.findViewById(R.id.hcf_uncaughtexception_button_send);

        if (reporter == null) {
            btnSend.setVisibility(View.GONE);
        } else {
            btnSend.setOnClickListener(view -> {
                Map<String, Object> additional = new TreeMap<>();
                additional.put(PARAM_RESULT, result);
                additional.put(PARAM_THROWABLE, throwable);
                reporter.report(scope, additional).whenDone(res -> {
                    Builder dlg = new Builder(activity);
                    if (res.exists()) {
                        dlg.setMessage(activity.getResources().getString(R.string.hcf_uncaughtexception_submitted, res.get()));
                    } else {
                        res.log();
                        dlg.setMessage(activity.getResources().getString(R.string.hcf_uncaughtexception_submission_failed));
                    }
                    dlg.setNeutralButton(R.string.hcf_action_ok, (dialog, which) -> dialog.dismiss());
                    dlg.show();
                });
            });
        }

        View icon = v.findViewById(R.id.hcf_uncaughtexception_image);
        if (icon instanceof MaterialFontView) {
            ((MaterialFontView) icon).setIcon(Icon.I_ERROR_OUTLINE);
        }
    }

    public static class BindUncaughtException extends ObjectBinding<UncaughtException> {

        private Throwable throwable;
        private Result<?> result;

        public BindUncaughtException(@Nullable Throwable throwable, @Nullable Result<?> result) {
            this.throwable = throwable;
            this.result = result;
        }

        @Nullable
        @Override
        protected UncaughtException onExecute() throws Exception {
            UncaughtException obj = new UncaughtException();
            obj.activity = get(Activity.class);
            obj.navigation = get(Navigation.class);
            obj.reporter = get(Reporter.class);
            obj.result = result;
            obj.scope = getScope();
            obj.throwable = throwable;
            return obj;
        }

        @Override
        protected void onPostExecute(SettableTask<Result<UncaughtException>> task, @Nullable UncaughtException e, @Nullable Throwable t) {
            if (t != null) {
                task.set(Result.create(e).setThrowable(t));
                return;
            }
            post(Android.NAME_MAIN_HANDLER, () -> {
                        try {
                            e.apply();
                        } catch (final Throwable e0) {
                            task.set(Result.create(e).setThrowable(e0));
                            return;
                        }
                        // the end of the call chain: tell the task that we are done
                        task.set(Result.create(e));
                    }
            );
        }
    }
}
