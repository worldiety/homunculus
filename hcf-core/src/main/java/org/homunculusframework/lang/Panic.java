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
package org.homunculusframework.lang;

/**
 * Indicates a programming error more explicitly, which indicates more a shutdown of the entire Program, because
 * the invariance of the running program cannot be recovered anymore.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Panic extends RuntimeException {
    public Panic(String msg) {
        super(msg);
    }

    public Panic(String msg, Throwable cause) {
        super(msg, cause);
    }

    public Panic(Throwable e) {
        super("", e);
    }

    public Panic() {
        super("");
    }
}
