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

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import javax.annotation.Nullable;

/**
 * A simple bitmap pool contract to make things easier for badly implemented Android VMs ("gc_for_alloc")
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface BitmapPool {
    /**
     * returns or creates a bitmap from the pool. It is guaranteed that the returned bitmap has the specified configuration, but may throw OOM.
     *
     * @param width
     * @param height
     * @param config
     * @return
     */
    Bitmap borrowBitmap(int width, int height, Config config);

    /**
     * returns a bitmap back into the pool
     *
     * @param bmp
     */
    void returnBitmap(@Nullable Bitmap bmp);

    /**
     * loops through the pool and returns the amount of bitmaps with the given config
     *
     * @param width
     * @param height
     * @param config
     * @return
     */
    int countBitmaps(int width, int height, Config config);


    /**
     * Releases all resources without destroying the pool
     */
    void clear();

    /**
     * releases all resources of the pool.
     */
    void destroy();
}
