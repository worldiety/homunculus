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
package org.homunculusframework.factory.flavor.hcf;

import org.homunculusframework.concurrent.Async;
import org.homunculusframework.factory.container.AnnotatedFieldProcessor;
import org.homunculusframework.factory.component.DefaultFactory;
import org.homunculusframework.factory.container.Container;
import org.homunculusframework.factory.serializer.Serializer;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Reflection;
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
public class HCFFieldPersistent implements AnnotatedFieldProcessor {
    private final File folder;
    private final Map<Class<? extends Serializer>, Serializer> serializers;


    public HCFFieldPersistent(File folder) {
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
                        LoggerFactory.getLogger(getClass()).warn("failed to save", e);
                    }
                }
            });


            try {
                Object resolvedValue = read(folder, key.name(), serializer, field.getType());
                //check if null and not resolvable -> try to create such an instance
                if (resolvedValue == null) {
                    Container container = scope.resolveNamedValue(Container.NAME_CONTAINER, Container.class);
                    if (container != null) {
                        //await has danger of deadlocks, especially for PostConstructs in main thread (which is the default case)
                        resolvedValue = Async.await(container.createComponent(scope, field.getType())).get();
                    }
                }

                field.set(instance, resolvedValue);
                LoggerFactory.getLogger(instance.getClass()).info("{}.{} = {}", instance.getClass().getSimpleName(), field.getName(), DefaultFactory.stripToString(resolvedValue));
            } catch (IllegalAccessException | IOException e) {
                LoggerFactory.getLogger(instance.getClass()).error("failed to deserialize: {}.{} -> {}", instance.getClass().getSimpleName(), field.getName(), e);
            }
        }
    }


    public static void save(File targetDir, String name, Serializer serializer, Class<?> type, Object obj) throws IOException {
        File dstFile = new File(targetDir, Reflection.getName(type) + "_" + name + "." + serializer.getId());
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
        try {
            tmp.createNewFile();
        } catch (IOException e) {
            dstFile.getParentFile().mkdirs();
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
    public static <T> T read(File targetDir, String name, Serializer serializer, Class<T> type) throws IOException {
        File dstFile = new File(targetDir, Reflection.getName(type) + "_" + name + "." + serializer.getId());
        if (dstFile.length() == 0) {
            return null;
        }

        FileInputStream fin = new FileInputStream(dstFile);
        try {
            BufferedInputStream bin = new BufferedInputStream(fin);
            Object res = serializer.deserialize(bin, type);
            return (T) res;
        } finally {
            fin.close();
        }
    }
}
