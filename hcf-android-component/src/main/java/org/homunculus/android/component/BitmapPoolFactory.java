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

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import org.homunculusframework.lang.Panic;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;



/**
 * A factory implementation
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class BitmapPoolFactory {

    private final static NoPool sNoPool = new NoPool();
    private final static BitmapPool sPool;
    private static boolean sPoolingEnabled = true;
    private static ByteBuffer sTmp = ByteBuffer.allocate(1024 * 1024 * 1);

    static {
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            sPool = new KitKat19AndroidPool();
        } else {
            sPool = new SimpleAndroidPool();
        }
    }

    public synchronized static BitmapPool getDefaultPool() {
        if (!sPoolingEnabled) return sNoPool;
        //large heap
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB)
            return sPool;
        else
            return sNoPool;
    }

    /**
     * Tries to recycle a pooled bitmap and reads raw pixels into the bitmap. Internally always reuses
     * a (potentially) big buffer to update the texture. The buffer grows to the largest size when needed and
     * is never shrunk, so be careful what you read, e.g. a fullhd frame will take more than 8 MiB. However to
     * lower the pressure to the stupid Android GC we sacrifice throughput by synchronizing globally, so NEVER
     * call this method from the UI thread.
     * <p>
     * The format is:
     * <pre>
     *     bool available   1 byte
     *     int width        4 byte
     *     int height       4 byte
     *     int type         1 byte, [2 = ARGB_8888]
     * </pre>
     *
     * @param in the source
     * @return the instance or null if not available
     * @throws IOException
     */
    @Nullable
    public static Bitmap readPixel(DataInput in) throws IOException {
        boolean available = in.readBoolean();
        if (!available) {
            return null;
        }
        int width = in.readInt();
        int height = in.readInt();
        int type = in.readUnsignedByte();
        if (type != Config.ARGB_8888.ordinal()) {
            throw new Panic("format not implemented " + type);
        }
        int bytes = width * height * 4;
        Bitmap bmp = getDefaultPool().borrowBitmap(width, height, Config.ARGB_8888);
        synchronized (BitmapPoolFactory.class) {
            if (sTmp.capacity() < bytes) {
                sTmp = ByteBuffer.allocate(bytes);
            }
            sTmp.clear();
            in.readFully(sTmp.array(), 0, bytes);
            sTmp.limit(bytes);
            bmp.copyPixelsFromBuffer(sTmp);
        }
        return bmp;
    }

    /**
     * The reverse function of {@link #readPixel(DataInput)}
     *
     * @param src the bitmap
     * @param dst the sink
     * @throws IOException
     */
    public static void writePixel(@Nullable Bitmap src, DataOutput dst) throws IOException {
        if (src != null && src.getConfig() != Config.ARGB_8888) {
            throw new Panic("only bitmaps of the type ARGB_8888 are supported");
        }
        dst.writeBoolean(src != null);
        if (src == null) {
            return;
        }
        dst.writeInt(src.getWidth());
        dst.writeInt(src.getHeight());
        int bytes = src.getWidth() * src.getHeight() * 4;
        dst.writeByte(src.getConfig().ordinal());
        synchronized (BitmapPoolFactory.class) {
            if (sTmp.capacity() < bytes) {
                sTmp = ByteBuffer.allocate(bytes);
            }
            sTmp.clear();
            sTmp.limit(bytes);
            src.copyPixelsToBuffer(sTmp);
            dst.write(sTmp.array(), 0, bytes);
        }
    }

    public static boolean isPoolingEnabled() {
        return sPoolingEnabled;
    }

    public synchronized static void disablePooling() {
        sPoolingEnabled = false;
        sPool.clear();
    }

    public static void enablePooling() {
        sPoolingEnabled = true;
    }

    public static BitmapPool getNoPool() {
        return sNoPool;
    }


    /**
     * All android pool implementations derive from this
     */
    public static abstract class AbsAndroidPool implements BitmapPool {
        public abstract void setDebug(boolean b);
    }

    public static class NoPool extends AbsAndroidPool {
        @Override
        public Bitmap borrowBitmap(int width, int height, Config config) {
            return Bitmap.createBitmap(width, height, config);
        }

        @Override
        public void returnBitmap(Bitmap bmp) {
            if (bmp != null && !bmp.isRecycled()) {
                Handler h = new Handler(Looper.getMainLooper());
                h.post(() -> {
                    h.post(() -> {
                        bmp.recycle();
                    });
                });
            }
        }

        @Override
        public void destroy() {

        }

        @Override
        public void clear() {

        }

        @Override
        public void setDebug(boolean b) {

        }

        @Override
        public int countBitmaps(int width, int height, Config config) {
            return 0;
        }
    }

    public static class SimpleAndroidPool extends AbsAndroidPool {
        /*
        around 8mb is an entire fullhd screen, we will cache just two screens at once, this is not sufficient for 4k which needs at least 32mb
         */
        private int mCapacity = 1024 * 1024 * 8 * 2;
        private LinkedList<Bitmap> mCache = new LinkedList<Bitmap>();
        private boolean mDebug;

        public void setDebug(boolean b) {
            mDebug = b;
        }

        @Override
        public Bitmap borrowBitmap(int width, int height, Config config) {
            synchronized (mCache) {
                Iterator<Bitmap> it = mCache.iterator();
                while (it.hasNext()) {
                    Bitmap bmp = it.next();
                    if (bmp.isRecycled()) {
                        it.remove();
                        if (mDebug)
                            LoggerFactory.getLogger(getClass()).debug("found already recycled bitmap {}", bmp);
                        continue;
                    }
                    if (bmp.getWidth() == width && bmp.getHeight() == height && bmp.getConfig() == config) {
                        it.remove();
                        if (mDebug)
                            LoggerFactory.getLogger(getClass()).debug("found bitmap {}x{}@{}, having {} entries", width, height, config, mCache.size());
                        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1) {
                            if (!bmp.hasAlpha()) {
                                bmp.setHasAlpha(true);
                            }
                        }
                        //important to clear, otherwise we would violate our "create" contract
                        bmp.eraseColor(0);
                        return bmp;
                    }
                }
            }

            Bitmap bmp = Bitmap.createBitmap(width, height, config);
            if (mDebug) {
                LoggerFactory.getLogger(getClass()).debug("cache miss {}x{}@{}, having {} entries", width, height, config, mCache.size());
                LoggerFactory.getLogger(getClass()).debug("created {}", bmp);
            }
            return bmp;
        }

        @Override
        public void clear() {
            synchronized (mCache) {
                for (Bitmap b : mCache) {
                    b.recycle();
                }
                mCache.clear();
            }
        }

        @Override
        public int countBitmaps(int width, int height, Config config) {
            int c = 0;
            synchronized (mCache) {
                Iterator<Bitmap> it = mCache.iterator();
                while (it.hasNext()) {
                    Bitmap bmp = it.next();
                    if (bmp.isRecycled()) {
                        it.remove();
                        continue;
                    }
                    if (bmp.getWidth() == width && bmp.getHeight() == height && bmp.getConfig() == config) {
                        c++;
                    }
                }
            }
            return c;
        }

        @Override
        public void returnBitmap(Bitmap bmp) {
            if (bmp == null || bmp.isRecycled())
                return;
            Handler h = new Handler(Looper.getMainLooper());
            h.post(() -> h.post(() -> {
                if (!bmp.isMutable()) {
                    LoggerFactory.getLogger(getClass()).warn("do not return immutable bitmaps!");
                    bmp.recycle();
                    return;
                }
                synchronized (mCache) {
                    if (mDebug)
                        checkIfAlreayIn(bmp);
                    int currentSize = getCurrentSize();
                    int additionalSize = bmp.getWidth() * bmp.getHeight() * 4;
                    if (additionalSize > 0.7f * mCapacity) {
                        if (mDebug)
                            LoggerFactory.getLogger(getClass()).debug("bitmap is larger than pool capacity {}", bmp);
                        bmp.recycle();
                        return;
                    }

                    while (additionalSize + currentSize > mCapacity && mCache.size() > 0) {
                        Bitmap toDel = mCache.remove();
                        additionalSize -= toDel.getWidth() * toDel.getHeight() * 4;
                        toDel.recycle();
                        if (mDebug)
                            LoggerFactory.getLogger(getClass()).debug("exceeding limit - free bitmap for {}x{}@{} {}", toDel.getWidth(), toDel.getHeight(), toDel.getConfig(), toDel);
                    }
                    if (mDebug)
                        LoggerFactory.getLogger(getClass()).debug("added to cache {}x{}@{} {}", bmp.getWidth(), bmp.getHeight(), bmp.getConfig(), bmp);
                    mCache.add(bmp);
                }
            }));
        }

        private void checkIfAlreayIn(Bitmap bmp) {
            for (Bitmap b : mCache)
                if (b == bmp)
                    throw new IllegalArgumentException("you cannot return bitmaps multiple times " + bmp);
        }

        private int getCurrentSize() {
            int size = 0;
            for (Bitmap bmp : mCache) {
                size += bmp.getWidth() * bmp.getHeight() * 4;
            }
            return size;
        }

        @Override
        public void destroy() {
            synchronized (mCache) {
                for (Bitmap bmp : mCache)
                    if (!bmp.isRecycled())
                        bmp.recycle();
                mCache.clear();
            }
        }
    }

    @TargetApi(VERSION_CODES.KITKAT)
    public static class KitKat19AndroidPool extends AbsAndroidPool {
        /*
        around 8mb is an entire fullhd screen, we will cache just two screens at once, this is not sufficient for 4k which needs at least 32mb
         */
        private int mCapacity = 1024 * 1024 * 8 * 2;
        private LinkedList<Bitmap> mCache = new LinkedList<Bitmap>();
        private boolean mDebug;

        private float mAcceptableMaxTrash = 1000;

        public void setDebug(boolean b) {
            mDebug = b;
        }


        @Override
        public Bitmap borrowBitmap(int width, int height, Config config) {
            if (config != Config.ARGB_8888) {
                throw new Panic("only argb8888 images are supported");
            }
            int expectedBytes = (width * height * 32) / 8;
            Bitmap bestMatch = null;
            int capacityDelta = Integer.MAX_VALUE;
            float trashPercent;
            synchronized (mCache) {
                Iterator<Bitmap> it = mCache.iterator();

                while (it.hasNext()) {
                    Bitmap bmp = it.next();
                    if (bmp.isRecycled()) {
                        it.remove();
                        if (mDebug)
                            LoggerFactory.getLogger(getClass()).debug("found already recycled bitmap {}", bmp);
                        continue;
                    }
                    int bytes = bmp.getAllocationByteCount();
                    if (bytes < expectedBytes) {
                        continue;
                    }
                    if (bestMatch == null || bytes - expectedBytes < capacityDelta) {
                        bestMatch = bmp;
                        capacityDelta = bytes - expectedBytes;
                    }
                }
                trashPercent = (capacityDelta / (float) expectedBytes) * 100;
                if (trashPercent < mAcceptableMaxTrash) {
                    mCache.remove(bestMatch);
                } else {
                    if (mDebug)
                        LoggerFactory.getLogger(getClass()).debug("Best bitmap is to inefficient: {}x{}@{}, having {} entries. Trash: {}%", width, height, config, mCache.size(), trashPercent);
                    bestMatch = null;
                }
            }


            if (bestMatch != null) {
                if (mDebug)
                    LoggerFactory.getLogger(getClass()).debug("found bitmap to reconfigure {}x{}@{}, having {} entries. Trash: {}%", width, height, config, mCache.size(), trashPercent);

                bestMatch.reconfigure(width, height, config);
                bestMatch.setHasAlpha(true);
                if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1) {
                    if (!bestMatch.hasAlpha()) {
                        bestMatch.setHasAlpha(true);
                    }
                }
                //important to clear, otherwise we would violate our "create" contract
                bestMatch.eraseColor(0);
                return bestMatch;
            }

            Bitmap bmp = Bitmap.createBitmap(width, height, config);
            bmp.setHasAlpha(true);
            if (mDebug) {
                LoggerFactory.getLogger(getClass()).debug("cache miss {}x{}@{}, having {} entries", width, height, config, mCache.size());
                LoggerFactory.getLogger(getClass()).debug("created {}", bmp);
            }
            return bmp;
        }

        @Override
        public void clear() {
            synchronized (mCache) {
                for (Bitmap b : mCache) {
                    b.recycle();
                }
                mCache.clear();
            }
        }

        @Override
        public int countBitmaps(int width, int height, Config config) {
            int c = 0;
            synchronized (mCache) {
                Iterator<Bitmap> it = mCache.iterator();
                while (it.hasNext()) {
                    Bitmap bmp = it.next();
                    if (bmp.isRecycled()) {
                        it.remove();
                        continue;
                    }
                    if (bmp.getWidth() == width && bmp.getHeight() == height && bmp.getConfig() == config) {
                        c++;
                    }
                }
            }
            return c;
        }

        @Override
        public void returnBitmap(Bitmap bmp) {
            if (bmp == null || bmp.isRecycled())
                return;
            final Handler h = new Handler(Looper.getMainLooper());
            h.post(() -> h.post(() -> {
                if (!bmp.isMutable()) {
                    LoggerFactory.getLogger(getClass()).warn("do not return immutable bitmaps!");
                    bmp.recycle();
                    return;
                }
                synchronized (mCache) {
                    if (mDebug)
                        checkIfAlreayIn(bmp);
                    int currentSize = getCurrentSize();
                    int additionalSize = bmp.getAllocationByteCount();
                    if (additionalSize > 0.7f * mCapacity) {
                        if (mDebug) {
                            LoggerFactory.getLogger(getClass()).debug("bitmap is larger than pool capacity {}", bmp);
                        }
                        bmp.recycle();
                        return;
                    }

                    while (additionalSize + currentSize > mCapacity && mCache.size() > 0) {
                        Bitmap toDel = mCache.remove();
                        additionalSize -= toDel.getWidth() * toDel.getHeight() * 4;
                        toDel.recycle();
                        if (mDebug)
                            LoggerFactory.getLogger(getClass()).debug("exceeding limit - free bitmap for {}x{}@{} {}", toDel.getWidth(), toDel.getHeight(), toDel.getConfig(), toDel);
                    }
                    if (mDebug)
                        LoggerFactory.getLogger(getClass()).debug("added to cache {}bytes {}", bmp.getAllocationByteCount(), bmp);
                    mCache.add(bmp);
                }
            }));
        }

        private void checkIfAlreayIn(Bitmap bmp) {
            for (Bitmap b : mCache)
                if (b == bmp)
                    throw new IllegalArgumentException("you cannot return bitmaps multiple times " + bmp);
        }

        private int getCurrentSize() {
            int size = 0;
            for (Bitmap bmp : mCache) {
                size += bmp.getWidth() * bmp.getHeight() * 4;
            }
            return size;
        }

        @Override
        public void destroy() {
            synchronized (mCache) {
                for (Bitmap bmp : mCache)
                    if (!bmp.isRecycled())
                        bmp.recycle();
                mCache.clear();
            }
        }
    }
}
