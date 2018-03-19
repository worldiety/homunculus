package org.homunculus.android.component.module.toolbarbuilder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.homunculus.android.component.Str;

/**
 * Created by aerlemann on 16.03.18.
 */

public class SuperToolbarBuilderTemplate {
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

    public SuperToolbarBuilderTemplate() {

    }

    public SuperToolbarBuilderTemplate(SuperToolbarBuilderTemplate template) {
        this.mToolbarColor = template.mToolbarColor;
        this.mTitleTextColor = template.mTitleTextColor;
        this.mBackgroundDrawable = template.mBackgroundDrawable;
        this.mTitleRes = template.mTitleRes;
        this.mMenuId = template.mMenuId;
        this.mShowNavigationIcons = template.mShowNavigationIcons;
        this.mToolbarTitleTextAppearanceContext = template.mToolbarTitleTextAppearanceContext;
        this.mToolbarTitleTextAppearance = template.mToolbarTitleTextAppearance;
        this.mToolbarSubTitleTextAppearanceContext = template.mToolbarSubTitleTextAppearanceContext;
        this.mToolbarSubTitleTextAppearance = template.mToolbarSubTitleTextAppearance;
        this.mToolbarLogoAsDrawable = template.mToolbarLogoAsDrawable;
        this.mToolbarLogoAsResource = template.mToolbarLogoAsResource;
    }

    public void setToolbarColor(Integer mToolbarColor) {
        this.mToolbarColor = mToolbarColor;
    }

    public void setTitleTextColor(Integer mTitleTextColor) {
        this.mTitleTextColor = mTitleTextColor;
    }

    public void setBackgroundDrawable(Drawable mBackgroundDrawable) {
        this.mBackgroundDrawable = mBackgroundDrawable;
    }

    public void setTitleRes(Str mTitleRes) {
        this.mTitleRes = mTitleRes;
    }

    public void setMenuId(Integer mMenuId) {
        this.mMenuId = mMenuId;
    }

    public void setShowNavigationIcons(boolean mShowNavigationIcons) {
        this.mShowNavigationIcons = mShowNavigationIcons;
    }

    public void setToolbarTitleTextAppearanceContext(Context mToolbarTitleTextAppearanceContext) {
        this.mToolbarTitleTextAppearanceContext = mToolbarTitleTextAppearanceContext;
    }

    public void setToolbarTitleTextAppearance(Integer mToolbarTitleTextAppearance) {
        this.mToolbarTitleTextAppearance = mToolbarTitleTextAppearance;
    }

    public void setToolbarSubTitleTextAppearanceContext(Context mToolbarSubTitleTextAppearanceContext) {
        this.mToolbarSubTitleTextAppearanceContext = mToolbarSubTitleTextAppearanceContext;
    }

    public void setToolbarSubTitleTextAppearance(Integer mToolbarSubTitleTextAppearance) {
        this.mToolbarSubTitleTextAppearance = mToolbarSubTitleTextAppearance;
    }

    public void setToolbarLogoAsDrawable(Drawable toolbarLogoAsDrawable) {
        this.mToolbarLogoAsDrawable = toolbarLogoAsDrawable;
    }

    public void setToolbarLogoAsResource(Integer mToolbarLogoAsResource) {
        this.mToolbarLogoAsResource = mToolbarLogoAsResource;
    }

    public void setCustomView(View mCustomView) {
        this.mCustomView = mCustomView;
    }
}
