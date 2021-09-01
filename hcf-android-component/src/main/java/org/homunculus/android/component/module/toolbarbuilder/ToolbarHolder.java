package org.homunculus.android.component.module.toolbarbuilder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import org.homunculusframework.factory.scope.LifecycleOwner;
import org.slf4j.LoggerFactory;

/**
 * Container class holding the toolbar and the content view (normally the remaining content on the screen).
 *
 * Created by aerlemann on 17.04.18.
 */

@SuppressLint("ViewConstructor")
public class ToolbarHolder<ContentView> extends LinearLayout {

    private ContentView mChild;
    private Toolbar mToolbar;

    public ToolbarHolder(LifecycleOwner owner, Context context, ContentView contentView, Toolbar toolbar) {
        super(context);
        mChild = contentView;
        mToolbar = toolbar;
        setOrientation(VERTICAL);
            /*
            Cut off all leaking bug from the android InputMethodManager regarding the toolbar, see also https://issuetracker.google.com/issues/37043700
            Otherwise Android leaks the entire UIS until the next toolbar and UIS does it's stuff.
            This bug has NOT been fixed since more than 3 year now!!!
             */
        if (owner != null) {
            owner.addDestroyCallback(o -> {
                mChild = null;
                mToolbar = null;
                removeAllViews();
            });
        } else {
            LoggerFactory.getLogger(getClass()).error("no owner scope, the Android InputMethodManager will leak your views");
        }
    }

    public ContentView getChild() {
        return mChild;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }
}