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

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;

import org.slf4j.LoggerFactory;



/**
 * Helper class to treat the soft keyboard and provides some missing bits.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class InputManager {

    private InputManager() {

    }

    /**
     * Hides the keyboard, if possible.
     */
    public static void hideSoftInput(@Nullable Activity act) {
        if (act != null && act.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(), 0);
            } else {
                LoggerFactory.getLogger(InputManager.class).error("INPUT_METHOD_SERVICE not available");
            }
        }
    }
}
