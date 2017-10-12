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

import android.content.Context;

/**
 * Android has a weired string resource management based on either strings, charsequences or integer resource ids.
 * At the end, you are ending up with a bunch of overloaded methods with a lot of duplicated code. This is a proposal
 * to work around this issue by providing a central class. It feels good, to statically import the str methods.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public final class Str {
    private final int rStr;
    private final String str;

    /**
     * Creates a new string resource from an android string resource. The actual value is resolved later.
     */
    public static Str str(int text) {
        return new Str(text, null);
    }

    /**
     * Creates a new string resource from a simple text.
     */
    public static Str str(String text) {
        return new Str(-1, text);
    }


    private Str(int rStr, String str) {
        this.rStr = rStr;
        this.str = str;
    }

    public String getString(Context ctx) {
        if (str == null) {
            if (rStr == 0xffffffff) {
                return "";
            }
            return ctx.getString(rStr);
        } else {
            return str;
        }
    }


}
