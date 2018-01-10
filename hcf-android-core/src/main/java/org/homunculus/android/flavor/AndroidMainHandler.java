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
package org.homunculus.android.flavor;

import android.os.Handler;
import android.os.Looper;

import org.homunculusframework.lang.Panic;

/**
 * The default typed Android main thread implementation.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class AndroidMainHandler implements org.homunculusframework.factory.container.Handler {
    private final Handler mHandler;

    public AndroidMainHandler() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void post(Runnable r) {
        mHandler.post(r);
    }

    /**
     * Returns true if the current thread is the main thread.
     *
     * @return true if the calling thread is the main thread
     */
    public static boolean isMainThread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    /**
     * Asserts that the current thread is the main thread
     */
    public static void assertMainThread() throws Panic {
        if (!isMainThread()) {
            throw new Panic("expected main thread but found " + Thread.currentThread());
        }
    }
}
