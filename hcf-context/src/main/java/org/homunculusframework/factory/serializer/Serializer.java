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
package org.homunculusframework.factory.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A simple serializer contract, useful for various cases.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public interface Serializer {

    /**
     * Returns true if the serialization was supported and has been applied. False if not supported, else the checked
     * exception of IOException to show that something else happend (e.g. disk full)
     */
    boolean serialize(Object src, OutputStream dst) throws IOException;

    /**
     * unmarshalls the object, usually into the given dst. Ma
     */
    Object deserialize(InputStream in, Class<?> type) throws IOException;

    /**
     * Returns a unique serializer id
     */
    String getId();
}
