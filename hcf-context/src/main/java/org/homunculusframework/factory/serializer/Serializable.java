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

import org.homunculusframework.lang.Panic;

import java.io.*;

/**
 * The default implementation of {@link java.io.Serializable}.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Serializable implements Serializer {
    @Override
    public boolean serialize(Object src, OutputStream dst) throws IOException {
        if (src instanceof java.io.Serializable) {
            ObjectOutputStream out = new ObjectOutputStream(dst);
            out.writeObject(src);
            out.flush();
            //we do not close the stream to avoid a closing chain
            return true;
        }
        return false;
    }

    @Override
    public Object deserialize(InputStream in, Class<?> type) throws IOException {
        ObjectInputStream oin = new ObjectInputStream(in);
        try {
            return oin.readObject();
        } catch (ClassNotFoundException e) {
            throw new Panic(e);
        }
    }

    @Override
    public String getId() {
        return "ser";
    }
}
