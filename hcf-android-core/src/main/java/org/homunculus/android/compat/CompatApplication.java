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

import org.homunculusframework.factory.scope.Scope;


/**
 * Just provides an application with some helper methods. It does NOT
 * provide a {@link org.homunculus.android.core.AndroidScopeContext} by overloading {@link android.content.ContextWrapper#attachBaseContext(Context)} as
 * suggested by the Android API Bug because of https://issuetracker.google.com/issues/37081588.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public abstract class CompatApplication extends Application {

    public abstract Scope getScope();
}
