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


import android.content.Context;
import org.homunculus.android.compat.ContextScope;
import org.homunculus.android.flavor.AndroidFlavor;
import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.factory.container.Configuration;
import org.homunculusframework.factory.flavor.ee.EEFlavor;
import org.homunculusframework.factory.flavor.hcf.HomunculusFlavor;
import org.homunculusframework.factory.flavor.spring.SpringFlavor;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.navigation.Navigation;
import org.homunculusframework.scope.Scope;

import java.io.File;

/**
 * A simple bootstrap helper class to setup HCF in an android environment. To make the usage of HC components simpler
 * and working out-of-the-box we create some framework code which makes some assumption for Android takes some decisions.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Android {

    /**
     * The name for a {@link Navigation}
     */
    public final static String NAME_NAVIGATION = "$navigation";
    /**
     * The name for a {@link Context}
     */
    public final static String NAME_CONTEXT = "$context";


    private Android() {
    }


}
