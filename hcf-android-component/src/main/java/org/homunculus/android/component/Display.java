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

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.homunculus.android.core.ContextScope;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A simple display helper class to give access to the current settings for a specific context. This class
 * is intentionally kept open to introduce additional or different behavior and scalings.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Display {
    public final static String NAME_DISPLAY = "$display";

    private final static Map<Context, Display> sDisplays = new WeakHashMap<>();

    private final DisplayMetrics mMetrics;

    protected Display(Context context) {
        mMetrics = context.getResources().getDisplayMetrics();
    }

    /**
     * Returns the display instance either from the context or from a static cache, connected to the context.
     *
     * @param context the context
     * @return the display, never null
     */
    public static Display from(Context context) {
        Display display = ContextScope.resolveNamedValue(context, NAME_DISPLAY, Display.class);
        if (display != null) {
            return display;
        }
        synchronized (sDisplays) {
            Display dp = sDisplays.get(context);
            if (dp == null) {
                dp = new Display(context);
                sDisplays.put(context, dp);
            }
            return dp;
        }
    }

    /**
     * Calculates the dip (Device Independent Pixels) into pixels
     *
     * @param dp the dp value
     * @return the rounded pixel value
     */
    public int dipToPix(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mMetrics);
    }

    /**
     * The reverse function of {@link #dipToPix(int)}
     *
     * @param px the pixel
     * @return the rounded dp value
     */
    public int pixToDip(int px) {
        return (int) (px / mMetrics.density);
    }

    /**
     * @return width in pixel (minus top- and bottom bar)
     */
    public int getWidth() {
        return mMetrics.widthPixels;
    }

    /**
     * @return height in pixel (minus top- and bottom bar)
     */
    public int getHeight() {
        return mMetrics.heightPixels;
    }
}
