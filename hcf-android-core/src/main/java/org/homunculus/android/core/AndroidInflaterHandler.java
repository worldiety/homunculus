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
package org.homunculus.android.core;

import android.os.HandlerThread;
import android.os.Looper;
import android.view.InflateException;
import org.homunculusframework.factory.container.Handler;
import org.slf4j.LoggerFactory;

/**
 * This is a fancy handler which allows inflating and creating views in other thread which are not the main thread.
 * Contraindications:
 * <ul>
 * <li>Some Samsung components (e.g. EditText) may have issues</li>
 * <li>The spinner (combobox) component is known to have layouting issues</li>
 * </ul>
 *
 * @author Torben Schinke
 * @author Heiko Lewin
 * @since 1.0
 */
public class AndroidInflaterHandler implements Handler {

    private final android.os.Handler mMainHandlerFallback;
    private final LooperThread[] mLoopers;
    private int mLooperIndex = 0;

    public AndroidInflaterHandler(int threads, int priority) {
        mMainHandlerFallback = new android.os.Handler(Looper.getMainLooper());
        mLoopers = new LooperThread[8];
        for (int i = 0; i < mLoopers.length; ++i) {
            mLoopers[i] = new LooperThread("AndroidInflaterHandler " + i, priority);
            mLoopers[i].start();
        }

    }

    @Override
    public void post(Runnable r) {
        int idx;
        synchronized (mLoopers) {
            idx = mLooperIndex;
            do {
                mLooperIndex = (mLooperIndex + 1) % mLoopers.length;
            }
            while (mLoopers[mLooperIndex].getLooper() == Looper.myLooper()); // never use the looper of the calling thread, but start round-robin
        }
        mLoopers[idx].getHandler().post(() -> {
            try {
                r.run();
            } catch (InflateException x) {
                LoggerFactory.getLogger(AndroidInflaterHandler.class).error("Could not inflate, trying fallback through main...", x);
                mMainHandlerFallback.post(r);
            } catch (RuntimeException x) {
                LoggerFactory.getLogger(AndroidInflaterHandler.class).error("Got exception while asyncViewInit", x);
                String msg = x.getMessage();
                if (msg != null && msg.contains("thread")) {
                    LoggerFactory.getLogger(AndroidInflaterHandler.class).error("...detected thread issue, trying fallback through main...", x);
                    mMainHandlerFallback.post(r);
                }
            }
        });
    }


    private static class LooperThread extends HandlerThread {
        private android.os.Handler mHandler;
        private Object mHandlerLock = new Object();

        public LooperThread(String name, int priority) {
            super(name, priority);
        }

        public android.os.Handler getHandler() {
            synchronized (mHandlerLock) {
                if (mHandler == null) {
                    mHandler = new android.os.Handler(getLooper());
                }
                return mHandler;
            }
        }
    }

}
