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

import android.app.Application;
import android.content.Context;
import org.homunculus.android.core.Android;
import org.homunculusframework.scope.Scope;

/**
 * Just provides an application with a {@link ContextScope} by overloading {@link android.content.ContextWrapper#attachBaseContext(Context)}
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class CompatApplication extends Application {

    private Scope mAppScope;

    @Override
    protected void attachBaseContext(Context base) {
        if (mAppScope != null) {
            mAppScope.destroy();
        }
        mAppScope = new Scope("/", null);
        mAppScope.putNamedValue(Android.NAME_CONTEXT, this);
        ContextScope ctx = new ContextScope(mAppScope, base);
        super.attachBaseContext(ctx);
    }
}
