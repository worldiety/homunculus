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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * The default implementation of {@link XMLEncoder} which should always work, using reflection. Probably the most
 * compatible implementation but probably also the slowest.
 * <p>
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Xml implements Serializer {
    @Override
    public boolean serialize(Object src, OutputStream dst) throws IOException {
        XMLEncoder encoder = new XMLEncoder(dst);
        encoder.writeObject(src);
        encoder.flush();
        encoder.close();
        return false;
    }

    @Override
    public <T> T deserialize(InputStream in, Class<T> type) throws IOException {
        XMLDecoder decoder = new XMLDecoder(in);
        return (T) decoder.readObject();
    }

    @Override
    public String getId() {
        return ".xml";
    }
}
