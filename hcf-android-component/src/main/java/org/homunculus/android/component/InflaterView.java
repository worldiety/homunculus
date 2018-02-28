package org.homunculus.android.component;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A basic class which is mainly used by code generator to create type safe views from XML files.
 * <p>
 * Created by Torben Schinke on 22.02.18.
 */
public abstract class InflaterView extends ViewGroup {
    private final View inflatedChild;

    public InflaterView(Context context, @LayoutRes int layoutId) {
        super(context);
        LayoutInflater li = LayoutInflater.from(getContext());
        inflatedChild = li.inflate(layoutId, null);
        super.addView(inflatedChild);
    }

    @Override
    public void setLayoutParams(LayoutParams params) {
        super.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        inflatedChild.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(inflatedChild.getMeasuredWidth(), inflatedChild.getMeasuredHeight());
    }

    @Override
    public void addView(View child) {
        throw new RuntimeException("adding views is not supported");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        inflatedChild.layout(l, t, r, b);
    }
}
