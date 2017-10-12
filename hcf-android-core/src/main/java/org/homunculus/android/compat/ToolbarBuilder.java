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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import org.homunculus.android.compat.ActivityEventDispatcher.AbsActivityEventCallback;
import org.homunculus.android.core.R;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.scope.OnBeforeDestroyCallback;
import org.homunculusframework.scope.Scope;


import java.util.Map;
import java.util.TreeMap;
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
public class ToolbarBuilder {

    private Integer mToolbarColor;
    private Integer mTitleTextColor;

    private Drawable mBackgroundDrawable;

    private View mCustomView;

    /**
     * Action which is performed when navigation "up" key is pressed
     */
    private Runnable mUpAction;

    /**
     * Menu id for actionbar menu, if null no menu is set
     */
    private Integer mMenuId;

    /**
     * Listener for clicks on menu
     */
    private Map<Integer, MenuItemClickListener> mItems = new TreeMap<>();

    /**
     * Title text
     */
    private Str mTitleRes;

    /**
     * Navigation drawer attached to the Start (left) border of the screen. Open through hamburger or by swipe gesture.
     */
    private View mLeftDrawer;

    /**
     * Content drawer attached to the end (right) border of the screen. Open through swipe gesture.
     */
    private View mRightDrawer;

    /**
     * Main ContentView
     */
    private View mContentView;

    /**
     * If true hamburger or back arrow will be displayed in toolbar.
     */
    private boolean mShowNavigationIcons = true;

    /**
     * The navigation drawer
     */
    private MyCustomDrawerLayout mDrawerLayout;

    private ToolbarContentConfiguratorListener toolbarContentConfiguratorListener;


    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    private ToolbarBuilder() {
    }

    public static ToolbarBuilder define() {
        return new ToolbarBuilder();
    }

    /**
     * Creates the toolbar and binds optionally the life cycle of it (like registered callbacks)
     * to the scope of the given context. See also {@link ContextScope}. Elements are cleared using
     * {@link Scope#addOnBeforeDestroyCallback(OnBeforeDestroyCallback)}
     */
    public <V extends View & ToolbarControl> V create(@Nullable Context context, EventAppCompatActivity activity) {
        MyCustomRelativeLayout contentLayout = new MyCustomRelativeLayout(activity);

        Scope scope = ContextScope.getScope(context);
        if (scope != null) {
            scope.addOnBeforeDestroyCallback(obj -> {
                mItems.clear();
                mItems = null;
            });
        }

        // Initialize menu
        initMenu(activity);
        // Initialize toolbar
        Toolbar toolbar = initToolbar(activity, contentLayout);
        // Initialize navigation drawer
        return initDrawerLayout(activity, (V) contentLayout, toolbar);
    }

    private void initMenu(final EventAppCompatActivity activity) {

        activity.getEventDispatcher().register(new AbsActivityEventCallback<EventAppCompatActivity>() {

            private boolean mIsInflated = false;

            @Override
            public void onActionModeStarted(ActionMode mode) {
                super.onActionModeStarted(mode);
                // Lock drawer while action mode is active
                if (mDrawerLayout != null) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }

            @Override
            public void onActionModeFinished(ActionMode mode) {
                super.onActionModeFinished(mode);
                // Unlock drawer when action mode is finished
                if (mDrawerLayout != null) {
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            }

            @Override
            public boolean onActivityCreateOptionsMenu(EventAppCompatActivity activity, Menu menu) {
                return invalidateOptionsMenu(menu);
            }

            @Override
            public boolean onActivityPrepareOptionsMenu(EventAppCompatActivity activity, Menu menu) {
                return invalidateOptionsMenu(menu);
            }

            private boolean invalidateOptionsMenu(Menu menu) {
                if (!mIsInflated) {
                    if (mMenuId != null) {
                        activity.getMenuInflater().inflate(mMenuId, menu);
                    } else {
                        menu.clear();
                    }
                    mIsInflated = true;
                    if (ToolbarBuilder.this.toolbarContentConfiguratorListener != null) {
                        ToolbarBuilder.this.toolbarContentConfiguratorListener.onMenuCreated(menu);
                    }
                    return true;
                }
                return mMenuId != null;
            }

            @Override
            public boolean onActivityOptionsItemSelected(EventAppCompatActivity activity, MenuItem item) {
                MenuItemClickListener itemListener = mItems.get(item.getItemId());
                if (itemListener != null) {
                    itemListener.onMenuItemSelected(item);
                }
                return super.onActivityOptionsItemSelected(activity, item);
            }
        });
        activity.supportInvalidateOptionsMenu();
    }

    @NonNull
    private Toolbar initToolbar(EventAppCompatActivity activity, RelativeLayout contentLayout) {
        Toolbar toolbar = new Toolbar(activity);

        if (mToolbarColor != null)
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, mToolbarColor));

        if (mTitleTextColor != null)
            toolbar.setTitleTextColor(ContextCompat.getColor(activity, mTitleTextColor));

        activity.setSupportActionBar(toolbar);
        ActionBar ab = activity.getSupportActionBar();

        if (ab == null) {
            throw new Panic("should not happen! actionBar was just set");
        }

        if (mBackgroundDrawable != null) {
            ab.setBackgroundDrawable(mBackgroundDrawable);
        }

        if (mCustomView != null) {
            ab.setCustomView(mCustomView);
        }

        if (mTitleRes != null) {
            ab.setTitle(mTitleRes.getString(activity));
            ab.setDisplayShowTitleEnabled(true);
        } else {
            ab.setDisplayShowTitleEnabled(false);
        }

        ab.setDisplayHomeAsUpEnabled(mShowNavigationIcons);

        int barSize = (int)contentLayout.getContext().getResources().getDimension(R.dimen.toolbarbuilder_barheight);
        RelativeLayout.LayoutParams toolbarLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, barSize);
        toolbar.setMinimumHeight(barSize);
        toolbar.setId(Widget.generateViewId());
        toolbar.setLayoutParams(toolbarLayoutParams);

        RelativeLayout.LayoutParams contentLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        contentLayoutParams.addRule(RelativeLayout.BELOW, toolbar.getId());
        mContentView.setLayoutParams(contentLayoutParams);

        contentLayout.addView(toolbar);
        contentLayout.addView(mContentView);

        return toolbar;
    }


    private <V extends View & ToolbarControl> V initDrawerLayout(EventAppCompatActivity activity, V contentLayout, Toolbar toolbar) {
        if (mUpAction != null) {
            toolbar.setNavigationOnClickListener(v -> mUpAction.run());
        }

        if (mLeftDrawer != null || mRightDrawer != null) {
            mDrawerLayout = new MyCustomDrawerLayout(activity);
            mDrawerLayout.addView(contentLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(activity, mDrawerLayout, R.string.app_name, R.string.app_name);

            if (mLeftDrawer != null) {
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.START;
                mDrawerLayout.addView(mLeftDrawer, params);

                toolbar.setNavigationOnClickListener(v -> {
                    if (mDrawerLayout.isDrawerOpen(mLeftDrawer)) {
                        mDrawerLayout.closeDrawer(mLeftDrawer);
                    } else {
                        mDrawerLayout.openDrawer(mLeftDrawer);
                    }
                });
                toggle.setDrawerIndicatorEnabled(true);
            } else {
                toggle.setDrawerIndicatorEnabled(false);
            }

            if (mRightDrawer != null) {
                LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.END;
                mDrawerLayout.setOnTouchListener((v, event) -> {
                    InputManager.hideSoftInput(activity);
                    return false;
                });
                mDrawerLayout.addView(mRightDrawer, params);
                if (mRightDrawer instanceof EventsDrawerView) {
                    ((EventsDrawerView) mRightDrawer).setParentDrawerLayout(mDrawerLayout);
                }

                // Add the drawer handle
                FrameLayout frameLayout = new ContentDrawerFrameLayout(activity);
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                toggle.syncState();
                return (V) frameLayout;
            }
            toggle.syncState();
            return (V) mDrawerLayout;
        }
        return contentLayout;
    }


    /**
     * Set the main content view
     *
     * @param contentView
     * @return
     */
    public ToolbarBuilder setContentView(View contentView) {
        mContentView = contentView;
        return this;
    }

    /**
     * Sets the left hand drawer view.
     * When setting this view the hamburger view will be displayed instead of back arrow if {@link ToolbarBuilder#setShowNavigationIcons(boolean)} was set to true.
     * <p>
     * Notice: When the view is in action mode, drawers are disabled
     *
     * @param navDrawer
     * @return
     */
    public ToolbarBuilder setNavDrawer(View navDrawer) {
        mLeftDrawer = navDrawer;
        return this;
    }

    /**
     * Sets the right hand drawer view.
     * <p>
     * Notice: When the view is in action mode, drawers are disabled
     *
     * @param contentDrawer
     * @return
     */
    public ToolbarBuilder setContentDrawer(View contentDrawer) {
        mRightDrawer = contentDrawer;
        return this;
    }

    /**
     * Set the toolbar background color
     *
     * @param toolbarColor
     * @return
     */
    public ToolbarBuilder setToolbarColor(Integer toolbarColor) {
        mToolbarColor = toolbarColor;
        return this;
    }

    /**
     * Sets the text color of the title, if present.
     *
     * @param textColor The new text color in 0xAARRGGBB formaterVideoDuration
     */
    public ToolbarBuilder setTitleTextColor(Integer textColor) {
        mTitleTextColor = textColor;
        return this;
    }

    public ToolbarBuilder setTitle(Str textRes) {
        mTitleRes = textRes;
        return this;
    }

    /**
     * Sets the action to be performed, when ActionBar's up icon is pressed
     *
     * @param upAction Runnable defining the navigating up action
     * @return The corresponding ToolBarContentConfigurator
     */
    public ToolbarBuilder setUpAction(Runnable upAction) {
        mUpAction = upAction;
        return this;
    }

    /**
     * Set the ActionBar's background.
     *
     * @param backgroundDrawable Background drawable
     */
    public ToolbarBuilder setBackgroundDrawable(Drawable backgroundDrawable) {
        mBackgroundDrawable = backgroundDrawable;
        return this;
    }

    public ToolbarBuilder setCustomView(View customView) {
        mCustomView = customView;
        return this;
    }

    /**
     * If true, navigation icons (hamburger, back arrow) will be displayed.
     *
     * @param showNavigationIcons
     * @return
     */
    public ToolbarBuilder setShowNavigationIcons(boolean showNavigationIcons) {
        mShowNavigationIcons = showNavigationIcons;
        return this;
    }

    /**
     * Use standard menu for actions which are bound to ui state, to provide a contextual menu use the action mode (See {@link ActionModeBuilder}.
     *
     * @param menuId resource ID of the menu (defined in /res/menu/)
     * @return
     */
    public ToolbarBuilder setMenuId(Integer menuId) {
        mMenuId = menuId;
        return this;
    }

    /**
     * Add an item to the ActionBar with a corresponding MenuItemClickListener.
     * Notice: This method only adds a {@link MenuItemClickListener} to a menuitem which need to be specified in a xml-menu (see {@link #setMenuId(Integer)}
     *
     * @param id            The resource ID of the menu item
     * @param clickListener The click listener for the menu item
     * @return The corresponding ToolBarContentConfigurator
     */
    public ToolbarBuilder addMenuItemListener(Integer id, MenuItemClickListener clickListener) {
        mItems.put(id, clickListener);
        return this;
    }

    /**
     * Use this to set a standard menu with corresponding click listeners for menu items from the menu
     *
     * @param menuId        Resource ID of the menu (defined in /res/menu/)
     * @param clicklistener Map containing resource IDs for menu items as keys and clicklistener for those items as values
     * @return
     */
    public ToolbarBuilder setMenu(Integer menuId, Map<Integer, MenuItemClickListener> clicklistener) {
        mMenuId = menuId;
        mItems.putAll(clicklistener);
        return this;
    }

    /**
     * Listener interface to handle item clicks in the menu toolbar
     */
    public interface MenuItemClickListener {

        /**
         * @param menuItem The menu item that was clicked
         * @return Boolean to evaluate item click
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
        public void onMenuCreated(Menu menu);
    }

    public ToolbarBuilder setToolbarContentConfiguratorListener(ToolbarContentConfiguratorListener toolbarContentConfiguratorListener) {
        this.toolbarContentConfiguratorListener = toolbarContentConfiguratorListener;
        return this;
    }

    private static class MyCustomRelativeLayout extends RelativeLayout implements ToolbarControl {

        public MyCustomRelativeLayout(Context context) {
            super(context);
        }

        @Override
        public void closeNavigationDrawer() {

        }

        @Override
        public void closeContentDrawer() {

        }
    }

    private class MyCustomDrawerLayout extends DrawerLayout implements ToolbarControl {

        public MyCustomDrawerLayout(Context context) {
            super(context);
        }

        @Override
        public void closeNavigationDrawer() {
            if (mLeftDrawer != null) {
                closeDrawer(mLeftDrawer);
            }
        }

        @Override
        public void closeContentDrawer() {
            if (mRightDrawer != null) {
                closeDrawer(mRightDrawer);
            }
        }
    }

    private class ContentDrawerFrameLayout extends FrameLayout implements ToolbarControl {

        public ContentDrawerFrameLayout(@NonNull Context context) {
            super(context);
            if (mDrawerLayout != null) {

                // Content drawer handle
                ImageView drawerHandle = new ImageView(context);
                FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                flParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                drawerHandle.setLayoutParams(flParams);
                drawerHandle.setImageResource(R.drawable.drawer_handle);

                // Add drawer layout with slide listener
                this.addView(mDrawerLayout);
                mDrawerLayout.addDrawerListener(new DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        if (drawerView == mRightDrawer) {
                            int offset = -1 * Math.round(slideOffset * mRightDrawer.getWidth());
                            drawerHandle.setTranslationX(offset);
                        }
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                    }
                });
                // Add the drawer handle
                this.addView(drawerHandle);
            }
        }

        @Override
        public void closeNavigationDrawer() {
            if (mDrawerLayout != null) {
                mDrawerLayout.closeNavigationDrawer();
            }
        }

        @Override
        public void closeContentDrawer() {
            if (mRightDrawer != null) {
                mDrawerLayout.closeContentDrawer();
            }
        }
    }

    public interface ToolbarControl {
        /**
         * Closes the left side of the drawer
         */
        void closeNavigationDrawer();

        /**
         * Closes the right side of the drawer
         */
        void closeContentDrawer();
    }
}
