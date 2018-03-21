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

import org.homunculusframework.factory.container.BackgroundHandler;
import org.homunculusframework.factory.container.Handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple one thread executor for the default background execution.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class AndroidBackgroundHandler implements BackgroundHandler {
    private final ExecutorService mExecutor;

    public AndroidBackgroundHandler(int threads, String name, int priority) {
        mExecutor = Executors.newFixedThreadPool(threads, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(name);
            thread.setPriority(priority);
            return thread;
        });
    }

    @Override
    public void post(Runnable r) {
        mExecutor.submit(r);
    }
}
