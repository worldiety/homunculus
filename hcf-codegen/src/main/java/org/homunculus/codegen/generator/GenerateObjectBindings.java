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
package org.homunculus.codegen.generator;

import org.homunculus.codegen.GenProject;
import org.homunculus.codegen.Generator;
import org.homunculus.codegen.generator.PreprocessDiscoverBeans.DiscoveryKind;
import org.homunculus.codegen.parse.FullQualifiedName;

/**
 * See {@link ObjectBindingGenerator}
 * Created by Torben Schinke on 05.03.18.
 */

public class GenerateObjectBindings implements Generator {
    @Override
    public void generate(GenProject project) throws Exception {
        for (FullQualifiedName bean : project.getDiscoveredKinds().get(DiscoveryKind.BEAN)) {
            new ObjectBindingGenerator().create(project, bean);
        }


    }


}
