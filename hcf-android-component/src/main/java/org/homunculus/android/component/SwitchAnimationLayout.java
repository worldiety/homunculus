package org.homunculus.android.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.homunculusframework.lang.Panic;


/**
 * Created by Torben Schinke on 07.11.17.
 */

public class SwitchAnimationLayout extends FrameLayout {
    @Nullable
    private View contentView;

    public SwitchAnimationLayout(@NonNull Context context) {
        super(context);
    }

    public SwitchAnimationLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchAnimationLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @param view
     * @param animationProvider
     */
    public void setContentView(@Nullable View view, AnimationProvider animationProvider) {

    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        throw new Panic("use setContentView");
    }

    public interface AnimationProvider {
        void applyAnimation(ViewGroup parent, @Nullable View oldView, @Nullable View newView);
    }

    public enum DefaultAnimation implements AnimationProvider {
        /**
         * No animation at all
         */
        REPLACE {
            @Override
            public void applyAnimation(ViewGroup parent, @Nullable View oldView, @Nullable View newView) {

            }
        },

        /**
         * A right to left slide animation, assuming the parents dimension
         */
        SLIDE_LEFT {
            @Override
            public void applyAnimation(ViewGroup parent, @Nullable View oldView, @Nullable View newView) {
                if (oldView != null) {
                    oldView.animate().translationX(-parent.getMeasuredWidth());
                }
                if (newView != null) {
                    newView.setTranslationX(-parent.getMeasuredWidth());
                    newView.animate().translationX(0);
                }
            }
        },

        /**
         * A left to right slide animation, assuming the parents dimension
         */
        SLIDE_RIGHT {
            @Override
            public void applyAnimation(ViewGroup parent, @Nullable View oldView, @Nullable View newView) {
                if (oldView != null) {
                    oldView.animate().translationX(parent.getMeasuredWidth());
                }
                if (newView != null) {
                    newView.setTranslationX(parent.getMeasuredWidth());
                    newView.animate().translationX(0);
                }
            }
        },

        /**
         * A cross fade transition
         */
        CROSS_FADE {
            @Override
            public void applyAnimation(ViewGroup parent, @Nullable View oldView, @Nullable View newView) {
                if (oldView != null) {
                    oldView.animate().alpha(0);
                }
                if (newView != null) {
                    newView.setAlpha(0f);
                    newView.animate().alpha(1);
                }
            }
        };


    }
}
