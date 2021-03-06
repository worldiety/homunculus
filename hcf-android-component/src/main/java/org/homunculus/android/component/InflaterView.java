package org.homunculus.android.component;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

/**
 * A basic class which is mainly used by code generator to create type safe views from XML files.
 * <p>
 * Created by Torben Schinke on 22.02.18.
 */
public abstract class InflaterView extends FrameLayout {

    public InflaterView(Context context, @LayoutRes int layoutId) {
        super(context);
        LayoutInflater li = LayoutInflater.from(getContext());
        super.addView(li.inflate(layoutId, null));
    }
    
}
