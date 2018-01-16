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
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.homunculus.android.core.ActivityEventDispatcher;
import org.homunculus.android.core.ActivityEventDispatcher.AbsActivityEventCallback;
import org.homunculus.android.core.ActivityEventOwner;
import org.homunculus.android.core.ContextScope;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.navigation.BackActionConsumer;
import org.homunculusframework.scope.OnBeforeDestroyCallback;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

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
    private ContentViewHolder mDrawerLayout;

    private ToolbarContentConfiguratorListener toolbarContentConfiguratorListener;
    private int generationId;

    private Context mToolbarTitleTextAppearanceContext;
    private Integer mToolbarTitleTextAppearance;

    private Context mToolbarSubTitleTextAppearanceContext;
    private Integer mToolbarSubTitleTextAppearance;

    //an activity scope shared generation id
    private final static String NGID = "toolbarBuilderGenerationId";
    private AtomicInteger nextGeneratedId;

    private Drawable toolbarLogoAsDrawable;
    private Integer toolbarLogoAsResource;

    private ToolbarBuilder() {
    }

    public static ToolbarBuilder define() {
        return new ToolbarBuilder();
    }

    /**
     * See {@link #create(Scope, AppCompatActivity, ActivityEventOwner, View, View, View)}
     */
    public <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> create(@Nullable Scope scope, AppCompatActivity activity, ActivityEventOwner owner, ContentView contentView) {
        return create(scope, activity, owner, contentView, null, null);
    }

    /**
     * See {@link #create(Scope, AppCompatActivity, ActivityEventOwner, View, View, View)}
     */
    public <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> create(@Nullable Scope scope, AppCompatActivity activity, ActivityEventOwner owner, ContentView contentView, @Nullable LeftDrawer leftDrawer) {
        return create(scope, activity, owner, contentView, leftDrawer, null);
    }

    /**
     * Creates the toolbar and binds optionally the life cycle of it (like registered callbacks)
     * to the scope of the given context. See also {@link org.homunculus.android.core.ContextScope}. Elements are cleared using
     * {@link Scope#addOnBeforeDestroyCallback(OnBeforeDestroyCallback)}
     * <p>
     */
    public <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> create(@Nullable Scope scope, AppCompatActivity activity, ActivityEventOwner owner, ContentView contentView, @Nullable LeftDrawer leftDrawer, @Nullable RightDrawer rightDrawer) {
        //only one active and valid toolbar builder is allowed per activity, so simply store the generation id in activity's scope. We use synchronized here in case of someone fiddles around with multiple inflater threads
        synchronized (ToolbarBuilder.class) {
            Scope activityScope = ContextScope.getScope(activity);
            if (activityScope == null) {
                //fallback
                nextGeneratedId = new AtomicInteger();
            } else {
                if (activityScope.has(NGID, AtomicInteger.class)) {
                    nextGeneratedId = activityScope.get(NGID, AtomicInteger.class);
                } else {
                    nextGeneratedId = new AtomicInteger();
                    activityScope.put(NGID, nextGeneratedId);
                }
            }
        }
        generationId = nextGeneratedId.incrementAndGet();
        if (scope != null) {
            scope.addOnBeforeDestroyCallback(obj -> {
                mItems.clear();
                mItems = null;
            });
        }

        mContentView = contentView;
        mLeftDrawer = leftDrawer;
        mRightDrawer = rightDrawer;
        // Initialize menu
        initMenu(scope, activity, owner.getEventDispatcher());
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

        dispatcher.register(scope, new AbsActivityEventCallback<Activity>() {


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
                if (mMenuId != null) {
                    activity.getMenuInflater().inflate(mMenuId, menu);
                }

                if (ToolbarBuilder.this.toolbarContentConfiguratorListener != null) {
                    ToolbarBuilder.this.toolbarContentConfiguratorListener.onMenuCreated(menu);
                }

                return true;
            }

            @Override
            public boolean onActivityOptionsItemSelected(Activity activity, MenuItem item) {
                if (isInvalidMenu()) {
                    logInvalidMenu();
                    return false;
                }
                MenuItemClickListener itemListener = mItems.get(item.getItemId());
                if (itemListener != null) {
                    itemListener.onMenuItemSelected(item);
                }
                return super.onActivityOptionsItemSelected(activity, item);
            }
        });
        activity.supportInvalidateOptionsMenu();
    }

    /**
     * See also {@link Toolbar#setTitleTextAppearance(Context, int)}
     *
     * @param context the context to derive the appearance
     * @param resId   the text appearance resource
     */
    public ToolbarBuilder setTitleTextAppearance(Context context, @StyleRes int resId) {
        mToolbarTitleTextAppearanceContext = context;
        mToolbarTitleTextAppearance = resId;
        return this;
    }

    /**
     * See also {@link Toolbar#setSubtitleTextAppearance(Context, int)}
     *
     * @param context the context to derive the appearance
     * @param resId   the text appearance resource
     */
    public ToolbarBuilder setSubTitleTextAppearance(Context context, @StyleRes int resId) {
        mToolbarSubTitleTextAppearanceContext = context;
        mToolbarSubTitleTextAppearance = resId;
        return this;
    }

    private ToolbarHolder initToolbar(AppCompatActivity activity) {
        Toolbar toolbar = new Toolbar(activity);
        int barSize = (int) activity.getResources().getDimension(R.dimen.toolbarbuilder_barheight);
        toolbar.setMinimumHeight(barSize);
        toolbar.setId(Widget.generateViewId());

        if (mToolbarColor != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, mToolbarColor));
        }

        if (mTitleTextColor != null) {
            toolbar.setTitleTextColor(ContextCompat.getColor(activity, mTitleTextColor));
        }


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


        ToolbarHolder holder = new ToolbarHolder(activity);
        holder.setId(Widget.generateViewId());
        holder.addView(toolbar, new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, barSize, 0));
        holder.addView(mContentView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1));

        holder.mToolbar = toolbar;
        holder.mChild = mContentView;


        if (mToolbarTitleTextAppearance != null) {
            Context ctx = mToolbarTitleTextAppearanceContext != null ? mToolbarTitleTextAppearanceContext : activity;
            toolbar.setTitleTextAppearance(ctx, mToolbarTitleTextAppearance);
        }

        if (mToolbarSubTitleTextAppearance != null) {
            Context ctx = mToolbarSubTitleTextAppearanceContext != null ? mToolbarSubTitleTextAppearanceContext : activity;
            toolbar.setTitleTextAppearance(ctx, mToolbarSubTitleTextAppearance);
        }
        return holder;
    }


    private ContentViewHolder<ToolbarHolder<?>, ?, ?> initDrawerLayout(AppCompatActivity activity, ToolbarHolder<View> contentLayout) {
        if (mUpAction != null) {
            contentLayout.getToolbar().setNavigationOnClickListener(v -> mUpAction.run());
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

        if (toolbarLogoAsResource != null) {
            contentLayout.getToolbar().setLogo(toolbarLogoAsResource);
        } else if (toolbarLogoAsDrawable != null) {
            contentLayout.getToolbar().setLogo(toolbarLogoAsDrawable);
        }


        return mDrawerLayout;
    }

    /**
     * Sets the toolbar logo.
     *
     * @param drawable the drawable
     * @return the builder
     */
    public ToolbarBuilder setLogo(Drawable drawable) {
        toolbarLogoAsDrawable = drawable;
        toolbarLogoAsResource = null;
        return this;
    }

    /**
     * Sets the toolbar logo.
     *
     * @param drawable the drawable
     * @return the builder
     */
    public ToolbarBuilder setLogo(@DrawableRes int drawable) {
        toolbarLogoAsDrawable = null;
        toolbarLogoAsResource = drawable;
        return this;
    }


    /**
     * Set the toolbar background color
     *
     * @param toolbarColor the color value
     * @return the builder
     */
    public ToolbarBuilder setToolbarColor(@ColorRes Integer toolbarColor) {
        mToolbarColor = toolbarColor;
        return this;
    }

    /**
     * Sets the text color of the title, if present.
     *
     * @param textColor The new text color
     */
    public ToolbarBuilder setTitleTextColor(@ColorRes Integer textColor) {
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
     * @param clickListener The clickOnce listener for the menu item
     * @return The corresponding ToolBarContentConfigurator
     */
    public ToolbarBuilder addMenuItemListener(Integer id, MenuItemClickListener clickListener) {
        mItems.put(id, clickListener);
        return this;
    }

    /**
     * Use this to set a standard menu with corresponding clickOnce listeners for menu items from the menu
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

    public ToolbarBuilder setToolbarContentConfiguratorListener(ToolbarContentConfiguratorListener toolbarContentConfiguratorListener) {
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
