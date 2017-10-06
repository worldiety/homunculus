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
package org.homunculusframework.factory.component;

import org.homunculusframework.factory.annotation.Persistent;
import org.homunculusframework.factory.serializer.Serializer;
import org.homunculusframework.lang.Classname;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.scope.OnAfterDestroyCallback;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies a simple persistence into a specific folder. The name is the class name + the persistent name.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class AFPPersistent implements AnnotatedFieldProcessor {
    private final File folder;
    private final Map<Class<? extends Serializer>, Serializer> serializers;


    public AFPPersistent(File folder) {
        this.folder = folder;
        this.serializers = new IdentityHashMap<>();
    }

    /**
     * registers a serializer with this processor.
     */
    public void putSerializer(Serializer serializer) {
        serializers.put(serializer.getClass(), serializer);
    }

    @Override
    public void process(Scope scope, Object instance, Field field) {
        Persistent key = field.getAnnotation(Persistent.class);
        if (key != null) {
            field.setAccessible(true);
            Serializer serializer = serializers.get(key.serializer());
            if (serializer == null) {
                LoggerFactory.getLogger(instance.getClass()).error("the serializer is unkown: " + key.serializer());
                return;
            }
            //we will save the entity if the scope gets destroyed
            scope.addOnAfterDestroyCallback(new OnAfterDestroyCallback() {
                @Override
                public void onAfterDestroy(Scope scope) {
                    try {
                        Object value = field.get(instance);
                        save(folder, key.name(), serializer, field.getType(), value);
                    } catch (IllegalAccessException e) {
                        throw new Panic(e);
                    } catch (IOException e) {
                        LoggerFactory.getLogger(getClass()).warn("failed to save");
                    }
                }
            });


            try {
                Object resolvedValue = read(folder, key.name(), serializer, field.getType());
                field.set(instance, resolvedValue);
                LoggerFactory.getLogger(instance.getClass()).debug("{}.{} = {}", instance.getClass().getSimpleName(), field.getName(), DefaultFactory.stripToString(resolvedValue));
            } catch (IllegalAccessException | IOException e) {
                LoggerFactory.getLogger(instance.getClass()).error("failed to deserialize: {}.{} -> {}", instance.getClass().getSimpleName(), field.getName(), e);
            }
        }
    }

    public static void save(File targetDir, String name, Serializer serializer, Class<?> type, Object obj) throws IOException {
        File dstFile = new File(targetDir, Classname.getName(type) + "_" + name + "." + serializer.getId());
        if (obj == null) {
            if (!dstFile.delete()) {
                if (dstFile.exists()) {
                    throw new IOException("cannot remove target file " + dstFile);
                } else {
                    return;
                }
            }
        }
        File tmp = new File(targetDir, UUID.randomUUID().toString());
        if (!tmp.createNewFile()) {
            dstFile.mkdirs();
            if (!tmp.createNewFile()) {
                throw new IOException("failed to allocate tmp file " + tmp);
            }
        }
        FileOutputStream fout = new FileOutputStream(tmp);
        try {
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            if (!serializer.serialize(obj, bout)) {
                throw new Panic("the serializer " + serializer + " rejected serialization");
            }
            bout.flush();
        } finally {
            fout.close();
        }

        if (!dstFile.delete()) {
            if (dstFile.exists()) {
                throw new IOException("unable to delete " + dstFile);
            }
        }
        if (!tmp.renameTo(dstFile)) {
            throw new IOException("unable to move " + tmp + " -> " + dstFile);
        }
    }

    @Nullable
    public static Object read(File targetDir, String name, Serializer serializer, Class<?> type) throws IOException {
        File dstFile = new File(targetDir, Classname.getName(type) + "_" + name + "." + serializer.getId());
        if (dstFile.length() == 0) {
            return null;
        }

        FileInputStream fin = new FileInputStream(dstFile);
        try {
            BufferedInputStream bin = new BufferedInputStream(fin);
            Object res = serializer.deserialize(bin, type);
            return res;
        } finally {
            fin.close();
        }
    }
}
