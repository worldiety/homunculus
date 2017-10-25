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
package org.homunculusframework.concurrent;

import java.util.LinkedList;

/**
 * A deferred execution list, which executes either when {@link #execute()} is called or when {@link #add(Runnable)} is called.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class ExecutionList {
    private final LinkedList<Runnable> jobs = new LinkedList<>();
    private boolean executed;

    /**
     * Adds another runnable. If execute has already been called, it is executed immediately.
     *
     * @param r
     */
    public void add(Runnable r) {
        synchronized (jobs) {
            if (executed) {
                r.run();
            } else {
                jobs.add(r);
            }
        }
    }

    /**
     * Executes all collected jobs once. Subsequent calls will have no effect. After executing all runnables this instance is de-facto free of leaks, and removes all contained runnables.
     */
    public void execute() {
        synchronized (jobs) {
            if (executed) {
                return;
            } else {
                try {
                    for (Runnable r : jobs) {
                        r.run();
                    }
                } finally {
                    executed = true;
                    jobs.clear();
                }
            }
        }
    }

    public boolean hasExecuted() {
        synchronized (jobs) {
            return executed;
        }
    }
}
