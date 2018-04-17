package org.homunculus.android.component.module.toolbarbuilder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StyleRes;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.homunculus.android.compat.EventAppCompatActivity;
import org.homunculus.android.component.ActionModeBuilder;
import org.homunculus.android.component.Str;
import org.homunculus.android.core.AndroidScopeContext;
import org.homunculusframework.factory.scope.Scope;
import org.homunculusframework.scope.OnDestroyCallback;

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nullable;

/**
 * Created by aerlemann on 16.03.18.
 */

public abstract class ToolbarConfiguration {
    private EventAppCompatActivity mAppCompatActivity;
    private Scope mScope;

    View mCustomView;

    Integer mToolbarColor;
    Integer mTitleTextColor;

    Drawable mBackgroundDrawable;

    /**
     * Title text
     */
    Str mTitleRes;

    /**
     * Menu id for actionbar menu, if null no menu is set
     */
    Integer mMenuId;

    /**
     * If true hamburger or back arrow will be displayed in toolbar.
     */
    boolean mShowNavigationIcons = true;

    Context mToolbarTitleTextAppearanceContext;
    Integer mToolbarTitleTextAppearance;

    Context mToolbarSubTitleTextAppearanceContext;
    Integer mToolbarSubTitleTextAppearance;

    Drawable mToolbarLogoAsDrawable;
    Integer mToolbarLogoAsResource;

    /**
     * Action which is performed when navigation "up" key is pressed
     */
    Runnable mUpAction;

    /**
     * Listener for clicks on menu
     */
    Map<Integer, MenuItemClickListener> mItems = new TreeMap<>();

    ToolbarContentConfiguratorListener mToolbarContentConfiguratorListener;

    Float mElevation;

    private ToolbarConfiguration() {

    }

    public ToolbarConfiguration(EventAppCompatActivity activity, Scope scope) {
        mAppCompatActivity = activity;
        mScope = scope;
        configure();
    }

    Scope getScope() {
        return mScope;
    }

    /**
     * Method to configure the parts of the Toolbar, which are default for this configuration. E.g. if your app has a logo in
     * every Toolbar, you may call {@link #setLogo(int)} here.
     */
    protected abstract void configure();

    /**
     * See {@link #createToolbar(View, View, View)}
     */
    public <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> createToolbar(ContentView contentView) {
        return new ToolbarCreator(this).create(mScope, mAppCompatActivity, contentView);
    }

    /**
     * See {@link #createToolbar(View, View, View)}
     */
    public <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> createToolbar(ContentView contentView, @Nullable LeftDrawer leftDrawer) {
        return new ToolbarCreator(this).create(mScope, mAppCompatActivity, contentView, leftDrawer);
    }

    /**
     * Creates the toolbar and binds optionally the life cycle of it (like registered callbacks)
     * to the scope of the given context. See also {@link AndroidScopeContext}. Elements are cleared using
     * {@link Scope#addDestroyCallback(OnDestroyCallback)}
     * <p>
     */
    public <ContentView extends View, LeftDrawer extends View, RightDrawer extends View> ContentViewHolder<ToolbarHolder<ContentView>, LeftDrawer, RightDrawer> createToolbar(ContentView contentView, @Nullable LeftDrawer leftDrawer, @Nullable RightDrawer rightDrawer) {
        return new ToolbarCreator(this).create(mScope, mAppCompatActivity, contentView, leftDrawer, rightDrawer);
    }

    /**
     * See also {@link Toolbar#setTitleTextAppearance(Context, int)}
     *
     * @param context the context to derive the appearance
     * @param resId   the text appearance resource
     */
    public ToolbarConfiguration setTitleTextAppearance(Context context, @StyleRes int resId) {
        this.mToolbarTitleTextAppearanceContext = mToolbarTitleTextAppearanceContext;
        this.mToolbarTitleTextAppearance = mToolbarTitleTextAppearance;
        return this;
    }

    /**
     * See also {@link Toolbar#setSubtitleTextAppearance(Context, int)}
     *
     * @param context the context to derive the appearance
     * @param resId   the text appearance resource
     */
    public ToolbarConfiguration setSubTitleTextAppearance(Context context, @StyleRes int resId) {
        this.mToolbarSubTitleTextAppearanceContext = mToolbarSubTitleTextAppearanceContext;
        this.mToolbarSubTitleTextAppearance = mToolbarSubTitleTextAppearance;
        return this;
    }

    /**
     * Sets the toolbar logo.
     *
     * @param drawable the drawable
     * @return the builder
     */
    public ToolbarConfiguration setLogo(Drawable drawable) {
        mToolbarLogoAsDrawable = drawable;
        mToolbarLogoAsResource = null;
        return this;
    }

    /**
     * Sets the toolbar logo.
     *
     * @param drawable the drawable
     * @return the builder
     */
    public ToolbarConfiguration setLogo(@DrawableRes int drawable) {
        mToolbarLogoAsDrawable = null;
        mToolbarLogoAsResource = drawable;
        return this;
    }

    /**
     * Set the toolbar background color
     *
     * @param toolbarColor the color value
     * @return the builder
     */
    public ToolbarConfiguration setToolbarColor(@ColorRes Integer toolbarColor) {
        mToolbarColor = toolbarColor;
        return this;
    }

    /**
     * Sets the text color of the title, if present.
     *
     * @param textColor The new text color
     */
    public ToolbarConfiguration setTitleTextColor(@ColorRes Integer textColor) {
        mTitleTextColor = textColor;
        return this;
    }

    public ToolbarConfiguration setTitle(Str textRes) {
        mTitleRes = textRes;
        return this;
    }

    /**
     * Set the ActionBar's background.
     *
     * @param backgroundDrawable Background drawable
     */
    public ToolbarConfiguration setBackgroundDrawable(Drawable backgroundDrawable) {
        mBackgroundDrawable = backgroundDrawable;
        return this;
    }

    public ToolbarConfiguration setCustomView(View customView) {
        mCustomView = customView;
        return this;
    }

    /**
     * If true, navigation icons (hamburger, back arrow) will be displayed.
     *
     * @param showNavigationIcons
     * @return
     */
    public ToolbarConfiguration setShowNavigationIcons(boolean showNavigationIcons) {
        mShowNavigationIcons = showNavigationIcons;
        return this;
    }

    /**
     * Use standard menu for actions which are bound to ui state, to provide a contextual menu use the action mode (See {@link ActionModeBuilder}.
     *
     * @param menuId resource ID of the menu (defined in /res/menu/)
     * @return
     */
    public ToolbarConfiguration setMenuId(Integer menuId) {
        mMenuId = menuId;
        return this;
    }

    /**
     * Sets the action to be performed, when ActionBar's up icon is pressed
     *
     * @param upAction Runnable defining the navigating up action
     * @return The corresponding ToolBarContentConfigurator
     */
    public ToolbarConfiguration setUpAction(Runnable upAction) {
        mUpAction = upAction;
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
    public ToolbarConfiguration addMenuItemListener(Integer id, MenuItemClickListener clickListener) {
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
    public ToolbarConfiguration setMenu(Integer menuId, Map<Integer, MenuItemClickListener> clicklistener) {
        mMenuId = menuId;
        mItems.putAll(clicklistener);
        return this;
    }

    /**
     * Sets the elevation for the {@link Toolbar}. Does nothing on devices < API-Level 21 (because this feature is not supported there).
     * See also {@link View#setElevation(float)}
     *
     * @param elevation the elevation
     * @return
     */
    public ToolbarConfiguration setElevation(float elevation) {
        mElevation = elevation;
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

    public ToolbarConfiguration setToolbarContentConfiguratorListener(ToolbarContentConfiguratorListener toolbarContentConfiguratorListener) {
        this.mToolbarContentConfiguratorListener = toolbarContentConfiguratorListener;
        return this;
    }

}
