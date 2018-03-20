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

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.InputManager;
import org.homunculus.android.component.R;
import org.homunculus.android.component.Widget;
import org.homunculus.android.core.ActivityEventDispatcher;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.navigation.BackActionConsumer;
import org.homunculusframework.scope.OnBeforeDestroyCallback;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;


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
    /**
     * Navigation drawer attached to the Start (left) border of the screen. Open through hamburger or by swipe gesture.
     */
    View mLeftDrawer;

    /**
     * Content drawer attached to the end (right) border of the screen. Open through swipe gesture.
     */
    View mRightDrawer;

    /**
     * Main ContentView
     */
    View mContentView;
    /**
     * The navigation drawer
     */
    private ContentViewHolder mDrawerLayout;

    private ToolbarContentConfiguratorListener toolbarContentConfiguratorListener;
    private int generationId;

    //an activity scope shared generation id
    private final static String NGID = "toolbarBuilderGenerationId";
    private AtomicInteger nextGeneratedId;

    private ToolbarConfiguration mToolbarConfiguration;

    ToolbarCreator(ToolbarConfiguration template) {
        mToolbarConfiguration = template;
    }

    /**
     * See {@link #create(Scope, EventAppCompatActivity, View, View, View)}
     */
    public <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> create(@Nullable Scope scope, EventAppCompatActivity activity, ContentView contentView) {
        return create(scope, activity, contentView, null, null);
    }

    /**
     * See {@link #create(Scope, EventAppCompatActivity, View, View, View)}
     */
    public <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> create(@Nullable Scope scope, EventAppCompatActivity activity, ContentView contentView, @Nullable LeftDrawer leftDrawer) {
        return create(scope, activity, contentView, leftDrawer, null);
    }

    /**
     * Creates the toolbar and binds optionally the life cycle of it (like registered callbacks)
     * to the scope of the given context. See also {@link org.homunculus.android.core.ContextScope}. Elements are cleared using
     * {@link Scope#addOnBeforeDestroyCallback(OnBeforeDestroyCallback)}
     * <p>
     */
    public <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> create(@Nullable Scope scope, EventAppCompatActivity activity, ContentView contentView, @Nullable LeftDrawer leftDrawer, @Nullable RightDrawer rightDrawer) {
//TODO!!!
//        //only one active and valid toolbar builder is allowed per activity, so simply store the generation id in activity's scope. We use synchronized here in case of someone fiddles around with multiple inflater threads
//        synchronized (ToolbarCreator.class) {
//            Scope activityScope = ContextScope.getScope(activity);
//            if (activityScope == null) {
//                //fallback
//                nextGeneratedId = new AtomicInteger();
//            } else {
//                if (activityScope.has(NGID, AtomicInteger.class)) {
//                    nextGeneratedId = activityScope.get(NGID, AtomicInteger.class);
//                } else {
//                    nextGeneratedId = new AtomicInteger();
//                    activityScope.put(NGID, nextGeneratedId);
//                }
//            }
//        }
//        generationId = nextGeneratedId.incrementAndGet();
//        if (scope != null) {
//            scope.addOnBeforeDestroyCallback(obj -> {
//                mToolbarTemplate.mItems.clear();
//                mToolbarTemplate.mItems = null;
//            });
//        }

        mContentView = contentView;
        mLeftDrawer = leftDrawer;
        mRightDrawer = rightDrawer;
        // Initialize menu
        initMenu(scope, activity, activity.getEventDispatcher());
        // Initialize toolbar
        ToolbarHolder toolbar = initToolbar(activity);
        // Initialize navigation drawer
        return initDrawerLayout(activity, toolbar);
    }

    private boolean isInvalidMenu() {
        return generationId != nextGeneratedId.get();
    }

    private void logInvalidMenu() {
        LoggerFactory.getLogger(getClass()).warn("Toolbar is already invalid");
    }

    private void initMenu(Scope scope, AppCompatActivity activity, final ActivityEventDispatcher dispatcher) {
//TODO!!!
//        dispatcher.register(scope, new AbsActivityEventCallback<Activity>() {
//
//
//            @Override
//            public void onActionModeStarted(ActionMode mode) {
//                if (isInvalidMenu()) {
//                    logInvalidMenu();
//                    return;
//                }
//                super.onActionModeStarted(mode);
//                // Lock drawer while action mode is active
//                if (mDrawerLayout != null) {
//                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//                }
//            }
//
//            @Override
//            public void onActionModeFinished(ActionMode mode) {
//                if (isInvalidMenu()) {
//                    logInvalidMenu();
//                    return;
//                }
//                super.onActionModeFinished(mode);
//                // Unlock drawer when action mode is finished
//                if (mDrawerLayout != null) {
//                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
//                }
//            }
//
//            @Override
//            public boolean onActivityCreateOptionsMenu(Activity activity, Menu menu) {
//                if (isInvalidMenu()) {
//                    logInvalidMenu();
//                    return false;
//                }
//                return invalidateOptionsMenu(menu);
//            }
//
//            @Override
//            public boolean onActivityPrepareOptionsMenu(Activity activity, Menu menu) {
//                if (isInvalidMenu()) {
//                    logInvalidMenu();
//                    return false;
//                }
//                return invalidateOptionsMenu(menu);
//            }
//
//            private boolean invalidateOptionsMenu(Menu menu) {
//                menu.clear();
//                if (mToolbarTemplate.mMenuId != null) {
//                    activity.getMenuInflater().inflate(mToolbarTemplate.mMenuId, menu);
//                }
//
//                if (ToolbarCreator.this.toolbarContentConfiguratorListener != null) {
//                    ToolbarCreator.this.toolbarContentConfiguratorListener.onMenuCreated(menu);
//                }
//
//                return true;
//            }
//
//            @Override
//            public boolean onActivityOptionsItemSelected(Activity activity, MenuItem item) {
//                if (isInvalidMenu()) {
//                    logInvalidMenu();
//                    return false;
//                }
//                MenuItemClickListener itemListener = mToolbarTemplate.mItems.get(item.getItemId());
//                if (itemListener != null) {
//                    itemListener.onMenuItemSelected(item);
//                }
//                return super.onActivityOptionsItemSelected(activity, item);
//            }
//        });
        activity.supportInvalidateOptionsMenu();
    }


    private ToolbarHolder initToolbar(AppCompatActivity activity) {
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


        ToolbarHolder holder = new ToolbarHolder(activity);
        holder.setId(Widget.generateViewId());
        holder.addView(toolbar, new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, barSize, 0));
        holder.addView(mContentView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));

        holder.mToolbar = toolbar;
        holder.mChild = mContentView;


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


    private ContentViewHolder<ToolbarHolder<?>, ?, ?> initDrawerLayout(AppCompatActivity activity, ToolbarHolder<View> contentLayout) {
        if (mToolbarConfiguration.mUpAction != null) {
            contentLayout.getToolbar().setNavigationOnClickListener(v -> mToolbarConfiguration.mUpAction.run());
        }
        mDrawerLayout = new ContentViewHolder(activity, contentLayout);

        //insert the left drawer
        if (mLeftDrawer != null) {
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.START;
            mDrawerLayout.addView(mLeftDrawer, params);

            contentLayout.getToolbar().setNavigationOnClickListener(v -> {
                if (mDrawerLayout.isDrawerOpen(mLeftDrawer)) {
                    mDrawerLayout.closeDrawer(mLeftDrawer);
                } else {
                    mDrawerLayout.openDrawer(mLeftDrawer);
                }
            });
        }

        //insert the right drawer
        if (mRightDrawer != null) {
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.END;
            mDrawerLayout.setOnTouchListener((v, event) -> {
                InputManager.hideSoftInput(activity);
                return false;
            });
            mDrawerLayout.addView(mRightDrawer, params);
        }

        //insert the drawer toggle
        if (mLeftDrawer != null || mRightDrawer != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, mDrawerLayout, R.string.toolbarbuilder_open_drawer_content_desc_res, R.string.toolbarbuilder_close_drawer_content_desc_res);
            if (mLeftDrawer != null) {
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


        return mDrawerLayout;
    }

    /**
     * Listener interface to handle item clicks in the menu toolbar
     */
    public interface MenuItemClickListener {

        /**
         * @param menuItem The menu item that was clicked
         * @return Boolean to evaluate item clickOnce
         */
        boolean onMenuItemSelected(MenuItem menuItem);
    }

    /**
     * Listener interface to get access to the created menu e.g. if you want to enable and disable menu items
     */
    public interface ToolbarContentConfiguratorListener {
        /**
         * @param menu The menu that was inflated
         */
        void onMenuCreated(Menu menu);
    }

    public ToolbarCreator setToolbarContentConfiguratorListener(ToolbarContentConfiguratorListener toolbarContentConfiguratorListener) {
        this.toolbarContentConfiguratorListener = toolbarContentConfiguratorListener;
        return this;
    }

    public static class ToolbarHolder<ContentView> extends LinearLayout {

        private ContentView mChild;
        private Toolbar mToolbar;

        public ToolbarHolder(Context context) {
            super(context);
            setOrientation(VERTICAL);
        }

        public ContentView getChild() {
            return mChild;
        }

        public Toolbar getToolbar() {
            return mToolbar;
        }
    }

    public class ContentViewHolder<ContentView extends View, LeftDrawer extends View, RightDrawer extends View> extends DrawerLayout implements BackActionConsumer {

        private ContentView mContentView;

        public ContentViewHolder(Context context, ContentView contentView) {
            super(context);
            mContentView = contentView;
            addView(mContentView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        public ContentView getContentView() {
            return mContentView;
        }

        @Nullable
        public LeftDrawer getLeftDrawer() {
            return (LeftDrawer) mLeftDrawer;
        }

        @Nullable
        public RightDrawer getRightDrawer() {
            return (RightDrawer) mRightDrawer;
        }

        public void closeLeftDrawer() {
            if (mLeftDrawer != null) {
                closeDrawer(mLeftDrawer);
            }
        }

        public void closeRightDrawer() {
            if (mRightDrawer != null) {
                closeDrawer(mRightDrawer);
            }
        }

        public void openLeftDrawer() {
            if (mLeftDrawer != null) {
                openDrawer(mLeftDrawer);
            }
        }

        public void openRightDrawer() {
            if (mRightDrawer != null) {
                openDrawer(mRightDrawer);
            }
        }

        public boolean isRightDrawerOpen() {
            if (mRightDrawer == null) {
                return false;
            }
            return mDrawerLayout.isDrawerOpen(mRightDrawer);
        }

        public boolean isLeftDrawerOpen() {
            if (mLeftDrawer == null) {
                return false;
            }
            return mDrawerLayout.isDrawerOpen(mLeftDrawer);
        }

        @Override
        public boolean backward() {
            if (isLeftDrawerOpen()) {
                closeLeftDrawer();
                return true;
            }
            if (isRightDrawerOpen()) {
                closeRightDrawer();
                return true;
            }
            return false;
        }
    }

}
