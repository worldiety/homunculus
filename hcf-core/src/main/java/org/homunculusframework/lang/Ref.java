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
 * A simple reference, just like the ref or out keyword of other languages.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Ref<T> implements Reference<T> {

    private T value;

    public Ref() {

    }

    public Ref(T value) {
        this.value = value;
    }

    public static <T> Ref<T> of(T value) {
        return new Ref(value);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }
}
