package org.homunculus.android.component.module.toolbarbuilder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

import org.homunculusframework.navigation.BackActionConsumer;


/**
 * Container class holding the content view (a {@link ToolbarHolder}) and optional views for the left and right drawer.
 *
 * Created by aerlemann on 17.04.18.
 */

@SuppressLint("ViewConstructor")
public class ContentViewHolder<ContentView extends View, LeftDrawer extends View, RightDrawer extends View> extends DrawerLayout implements BackActionConsumer {

    /**
     * Main ContentView
     */
    private ContentView mContentView;

    /**
     * Navigation drawer attached to the Start (left) border of the screen. Open through hamburger or by swipe gesture.
     */
    private LeftDrawer mLeftDrawer;

    /**
     * Content drawer attached to the end (right) border of the screen. Open through swipe gesture.
     */
    private RightDrawer mRightDrawer;

    public ContentViewHolder(Context context, ContentView contentView, LeftDrawer leftDrawer, RightDrawer rightDrawer) {
        super(context);
        mContentView = contentView;
        mLeftDrawer = leftDrawer;
        mRightDrawer = rightDrawer;
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
        return isDrawerOpen(mRightDrawer);
    }

    public boolean isLeftDrawerOpen() {
        if (mLeftDrawer == null) {
            return false;
        }
        return isDrawerOpen(mLeftDrawer);
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
