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
package org.homunculus.android.component.module.toolbarbuilder;

import android.app.Activity;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.drawerlayout.widget.DrawerLayout.LayoutParams;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.InputManager;
import org.homunculus.android.component.R;
import org.homunculus.android.component.Widget;
import org.homunculus.android.component.module.toolbarbuilder.ToolbarConfiguration.MenuItemClickListener;
import org.homunculus.android.core.ActivityCallback;
import org.homunculus.android.core.ActivityEventDispatcher;
import org.homunculus.android.core.ActivityEventDispatcher.AbsActivityEventCallback;
import org.homunculus.android.core.AndroidScopeContext;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Reference;
import org.homunculusframework.scope.LifecycleEntry;
import org.homunculusframework.scope.OnDestroyCallback;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;



/**
 * A builder around the {@link Toolbar} and the {@link DrawerLayout} to support left and right menus in the optional
 * world of {@link Scope}s. Also it allows a component oriented approach which is currently impossible using
 * pure Android Activities.
 *
 * @author Torben Schinke
 * @author Dennis Pilny
 * @since 1.0
 */
class ToolbarCreator {
    private int generationId;

    //an activity scope shared generation id
    private final static String NGID = "toolbarBuilderGenerationId";
    private AtomicInteger nextGeneratedId;

    /**
     * The navigation drawer
     */
    private ContentViewHolder mDrawerLayout;


    private ToolbarConfiguration mToolbarConfiguration;

    ToolbarCreator(ToolbarConfiguration template) {
        mToolbarConfiguration = template;
    }

    /**
     * See {@link #create(Scope, EventAppCompatActivity, View, View, View)}
     */
    <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> create(@Nullable Scope scope, EventAppCompatActivity activity, ContentView contentView) {
        return create(scope, activity, contentView, null, null);
    }

    /**
     * See {@link #create(Scope, EventAppCompatActivity, View, View, View)}
     */
    <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> create(@Nullable Scope scope, EventAppCompatActivity activity, ContentView contentView, @Nullable LeftDrawer leftDrawer) {
        return create(scope, activity, contentView, leftDrawer, null);
    }

    /**
     * Creates the toolbar and binds optionally the life cycle of it (like registered callbacks)
     * to the scope of the given context. See also {@link AndroidScopeContext}. Elements are cleared using
     * {@link Scope#addDestroyCallback(OnDestroyCallback)}
     * <p>
     */
    <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> create(@Nullable Scope scope, EventAppCompatActivity activity, ContentView contentView, @Nullable LeftDrawer leftDrawer, @Nullable RightDrawer rightDrawer) {


        //only one active and valid toolbar builder is allowed per activity, so simply store the generation id in activity's scope. We use synchronized here in case of someone fiddles around with multiple inflater threads
        synchronized (ToolbarCreator.class) {
            Scope activityScope = AndroidScopeContext.getScope(activity);
            if (activityScope == null) {
                //fallback
                nextGeneratedId = new AtomicInteger();
            } else {
                Reference<AtomicInteger> ctr = LifecycleEntry.get(activityScope, NGID, AtomicInteger.class);
                if (ctr.get() == null) {
                    ctr.set(new AtomicInteger());
                }
                nextGeneratedId = ctr.get();
            }
        }
        generationId = nextGeneratedId.incrementAndGet();
        if (scope != null) {
            scope.addDestroyCallback(obj -> {
                mToolbarConfiguration.mItems.clear();
                mToolbarConfiguration.mItems = null;
            });
        }

        // Initialize menu
        initMenu(scope, activity, activity.getEventDispatcher());
        // Initialize toolbar
        ToolbarHolder toolbar = initToolbar(scope, activity, contentView);
        // Initialize navigation drawer
        return initDrawerLayout(activity, toolbar, leftDrawer, rightDrawer);
    }

    private boolean isInvalidMenu() {
        return generationId != nextGeneratedId.get();
    }

    private void logInvalidMenu() {
        LoggerFactory.getLogger(getClass()).warn("Toolbar is already invalid");
    }

    private void initMenu(Scope scope, AppCompatActivity activity, final ActivityEventDispatcher dispatcher) {
//TODO!!!
        ActivityCallback callback = new ActivityCallback(scope,dispatcher);
        callback.setDelegate( new AbsActivityEventCallback<Activity>() {


            @Override
            public void onActionModeStarted(ActionMode mode) {
                if (isInvalidMenu()) {
                    logInvalidMenu();
                    return;
                }
                super.onActionModeStarted(mode);
                // Lock drawer while action mode is active
                if (mDrawerLayout != null) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }

            @Override
            public void onActionModeFinished(ActionMode mode) {
                if (isInvalidMenu()) {
                    logInvalidMenu();
                    return;
                }
                super.onActionModeFinished(mode);
                // Unlock drawer when action mode is finished
                if (mDrawerLayout != null) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            }

            @Override
            public boolean onActivityCreateOptionsMenu(Activity activity, Menu menu) {
                if (isInvalidMenu()) {
                    logInvalidMenu();
                    return false;
                }
                return invalidateOptionsMenu(menu);
            }

            @Override
            public boolean onActivityPrepareOptionsMenu(Activity activity, Menu menu) {
                if (isInvalidMenu()) {
                    logInvalidMenu();
                    return false;
                }
                return invalidateOptionsMenu(menu);
            }

            private boolean invalidateOptionsMenu(Menu menu) {
                menu.clear();
                if (mToolbarConfiguration.mMenuId != null) {
                    activity.getMenuInflater().inflate(mToolbarConfiguration.mMenuId, menu);
                }

                if (mToolbarConfiguration.mToolbarContentConfiguratorListener != null) {
                    mToolbarConfiguration.mToolbarContentConfiguratorListener.onMenuCreated(menu);
                }

                return true;
            }

            @Override
            public boolean onActivityOptionsItemSelected(Activity activity, MenuItem item) {
                if (isInvalidMenu()) {
                    logInvalidMenu();
                    return false;
                }
                MenuItemClickListener itemListener = mToolbarConfiguration.mItems.get(item.getItemId());
                if (itemListener != null) {
                    itemListener.onMenuItemSelected(item);
                }
                return super.onActivityOptionsItemSelected(activity, item);
            }
        });
        activity.supportInvalidateOptionsMenu();
    }


    private <ContentView extends View> ToolbarHolder<ContentView> initToolbar(Scope scope, AppCompatActivity activity, ContentView contentView) {
        Toolbar toolbar = new Toolbar(activity);
        int barSize = (int) activity.getResources().getDimension(R.dimen.toolbarbuilder_barheight);
        toolbar.setMinimumHeight(barSize);
        toolbar.setId(Widget.generateViewId());

        if (mToolbarConfiguration.mToolbarColor != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, mToolbarConfiguration.mToolbarColor));
        }

        if (mToolbarConfiguration.mTitleTextColor != null) {
            toolbar.setTitleTextColor(ContextCompat.getColor(activity, mToolbarConfiguration.mTitleTextColor));
        }

        if (mToolbarConfiguration.mElevation != null) {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                toolbar.setElevation(mToolbarConfiguration.mElevation);
            }
        }

        activity.setSupportActionBar(toolbar);
        ActionBar ab = activity.getSupportActionBar();

        if (ab == null) {
            throw new Panic("should not happen! actionBar was just set");
        }

        if (mToolbarConfiguration.mBackgroundDrawable != null) {
            ab.setBackgroundDrawable(mToolbarConfiguration.mBackgroundDrawable);
        }

        if (mToolbarConfiguration.mCustomView != null) {
            ab.setCustomView(mToolbarConfiguration.mCustomView);
        }

        if (mToolbarConfiguration.mTitleRes != null) {
            ab.setTitle(mToolbarConfiguration.mTitleRes.getString(activity));
            ab.setDisplayShowTitleEnabled(true);
        } else {
            ab.setDisplayShowTitleEnabled(false);
        }

        ab.setDisplayHomeAsUpEnabled(mToolbarConfiguration.mShowNavigationIcons);

        ToolbarHolder<ContentView> holder = new ToolbarHolder<>(scope, activity, contentView, toolbar);
        holder.setId(Widget.generateViewId());
        holder.addView(toolbar, new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, barSize, 0));
        holder.addView(contentView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));

        if (mToolbarConfiguration.mToolbarTitleTextAppearance != null) {
            Context ctx = mToolbarConfiguration.mToolbarTitleTextAppearanceContext != null ? mToolbarConfiguration.mToolbarTitleTextAppearanceContext : activity;
            toolbar.setTitleTextAppearance(ctx, mToolbarConfiguration.mToolbarTitleTextAppearance);
        }

        if (mToolbarConfiguration.mToolbarSubTitleTextAppearance != null) {
            Context ctx = mToolbarConfiguration.mToolbarSubTitleTextAppearanceContext != null ? mToolbarConfiguration.mToolbarSubTitleTextAppearanceContext : activity;
            toolbar.setTitleTextAppearance(ctx, mToolbarConfiguration.mToolbarSubTitleTextAppearance);
        }
        return holder;
    }


    private <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> initDrawerLayout(AppCompatActivity activity, ToolbarHolder<ContentView> contentLayout, LeftDrawer leftDrawer, RightDrawer rightDrawer) {
        if (mToolbarConfiguration.mUpAction != null) {
            contentLayout.getToolbar().setNavigationOnClickListener(v -> mToolbarConfiguration.mUpAction.run());
        }
        ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> drawerLayout = new ContentViewHolder<>(activity, contentLayout, leftDrawer, rightDrawer);
        mDrawerLayout = drawerLayout;

        //insert the left drawer
        if (leftDrawer != null) {
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.START;
            drawerLayout.addView(leftDrawer, params);

            contentLayout.getToolbar().setNavigationOnClickListener(v -> {
                if (drawerLayout.isDrawerOpen(leftDrawer)) {
                    drawerLayout.closeDrawer(leftDrawer);
                } else {
                    drawerLayout.openDrawer(leftDrawer);
                }
            });
        }

        //insert the right drawer
        if (rightDrawer != null) {
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.END;
            drawerLayout.setOnTouchListener((v, event) -> {
                InputManager.hideSoftInput(activity);
                return false;
            });
            drawerLayout.addView(rightDrawer, params);
        }

        //insert the drawer toggle
        if (leftDrawer != null || rightDrawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, drawerLayout, R.string.toolbarbuilder_open_drawer_content_desc_res, R.string.toolbarbuilder_close_drawer_content_desc_res);
            if (leftDrawer != null) {
                toggle.setDrawerIndicatorEnabled(true);
            } else {
                toggle.setDrawerIndicatorEnabled(false);
            }
            toggle.syncState();
        }

        if (mToolbarConfiguration.mToolbarLogoAsResource != null) {
            contentLayout.getToolbar().setLogo(mToolbarConfiguration.mToolbarLogoAsResource);
        } else if (mToolbarConfiguration.mToolbarLogoAsDrawable != null) {
            contentLayout.getToolbar().setLogo(mToolbarConfiguration.mToolbarLogoAsDrawable);
        }


        return drawerLayout;
    }

}
