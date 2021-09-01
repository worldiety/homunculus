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

import android.content.Context;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;

import org.homunculus.android.core.AndroidScopeContext;
import org.homunculusframework.factory.scope.Scope;

import java.util.Map;
import java.util.TreeMap;


/**
 * A builder around the {@link ActionMode} from Android to simplify the usage and to integrate it into the world
 * of {@link Scope}s transparently. The scope API is optional and hidden behind {@link AndroidScopeContext}.
 * After the action mode has been destroyed, all configured references are removed.
 *
 * @author Torben Schinke
 * @author Dennis Pilny
 * @since 1.0
 */
public class ActionModeBuilder {

    private ActionMode mActionMode;
    private Integer mActionMenuId;
    private View mCustomView;
    private Str mTitle;
    private Str mSubtitle;
    private Map<Integer, ActionListener> mActions = new TreeMap<>();
    private DestroyListener mDestroyListener;

    private ActionModeBuilder() {
    }

    public static ActionModeBuilder define() {
        return new ActionModeBuilder();
    }

    public ActionModeBuilder setTitle(Str title) {
        mTitle = title;
        return this;
    }

    public ActionModeBuilder setSubtitle(Str subtitle) {
        mSubtitle = subtitle;
        return this;
    }

    public ActionModeBuilder setOnDestroyListener(DestroyListener destroyListener) {
        mDestroyListener = destroyListener;
        return this;
    }

    public ActionModeBuilder setCustomView(View customView) {
        mCustomView = customView;
        return this;
    }

    public ActionModeBuilder addAction(int actionId, ActionListener listener) {
        mActions.put(actionId, listener);
        return this;
    }

    public ActionModeBuilder setActionMenuId(Integer actionMenuId) {
        mActionMenuId = actionMenuId;
        return this;
    }

    public ActionModeBuilder setActionMenu(Integer actionMenuId, Map<Integer, ActionListener> actions) {
        mActionMenuId = actionMenuId;
        if (actions != null) {
            mActions.putAll(actions);
        }
        return this;
    }

    /**
     * Tries to automatically detect the {@link Scope} from the view's context. Works also without any scope.
     * See also {@link AndroidScopeContext}.
     */
    public ActionMode create(View view) {
        return create(AndroidScopeContext.getScope(view.getContext()), view);
    }

    /**
     * If scope is not null, the action mode is finished just right before the scope is destroyed.
     * See also {@link #create(View)}
     */
    public ActionMode create(@Nullable Scope scope, View view) {
        Context context = view.getContext();

        if (scope != null) {
            scope.addDestroyCallback(s -> {
                if (mActionMode != null) {
                    mActionMode.finish();
                }
            });
        }
        String title = mTitle != null ? mTitle.getString(context) : null;
        String subtitle = mSubtitle != null ? mSubtitle.getString(context) : null;

        mActionMode = view.startActionMode(new Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                if (mActionMenuId != null) {
                    inflater.inflate(mActionMenuId, menu);
                }

                if (mCustomView != null) {
                    mode.setCustomView(mCustomView);
                } else {
                    if (title != null) {
                        mode.setTitle(title);
                    }

                    if (subtitle != null) {
                        mode.setSubtitle(subtitle);
                    }
                }

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                ActionListener listener = mActions.get(item.getItemId());
                if (listener != null) {
                    return listener.onActionItemClicked(mode, item);
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                if (mDestroyListener != null) {
                    mDestroyListener.onDestroyActionMode(mode);
                    mDestroyListener = null;
                }
                mActions.clear();
                mActionMode = null;
            }
        });
        return mActionMode;
    }

    public interface ActionListener {
        boolean onActionItemClicked(ActionMode mode, MenuItem item);
    }

    public interface DestroyListener {
        void onDestroyActionMode(ActionMode mode);
    }
}
