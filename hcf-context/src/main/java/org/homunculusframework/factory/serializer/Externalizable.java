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
import java.lang.reflect.InvocationTargetException;

/**
 * The default implementation of {@link java.io.Externalizable}. Probably one of the fastest possible.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Externalizable implements Serializer {
    @Override
    public boolean serialize(Object src, OutputStream dst) throws IOException {
        if (src instanceof java.io.Externalizable) {
            ObjectOutputStream out = new ObjectOutputStream(dst);
            ((java.io.Externalizable) src).writeExternal(out);
            out.flush();
            //we do not close the stream to avoid a closing chain
            return true;
        }
        return false;
    }

    @Override
    public <T> T deserialize(InputStream in, Class<T> type) throws IOException {
        if (java.io.Externalizable.class.isAssignableFrom(type)) {
            //using this serializer without the contract is a developer fault and must be punished
            throw new Panic("the type " + type + " must implement java.io.Externalizable");
        }
        java.io.Externalizable newInstance;
        try {
            newInstance = (java.io.Externalizable) type.getConstructor().newInstance();
            //cannot use InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException because of android incompatibility
        } catch (Exception e) {
            //using this serializer without empty constructor is a developer fault and must be punished
            throw new Panic("tried to deserialize " + type + " which must provide a public (static) and empty constructor", e);
        }

        ObjectInputStream oin = new ObjectInputStream(in);
        try {
            newInstance.readExternal(oin);
        } catch (ClassNotFoundException e) {
            throw new Panic(e);
        }
        return (T) newInstance;
    }

    @Override
    public String getId() {
        return "ext";
    }
}
