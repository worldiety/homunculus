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
import org.homunculusframework.lang.Reference;
import org.homunculusframework.lang.Reflection;
import org.homunculusframework.scope.OnAfterDestroyCallback;
import org.homunculusframework.scope.Scope;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
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

            //we will save the entity if the scope gets destroyed
            scope.addOnAfterDestroyCallback(new OnAfterDestroyCallback() {
                @Override
                public void onAfterDestroy(Scope scope) {
                    save(key, instance, field);
                }
            });


            load(scope, key, instance, field);
        }
    }

    public void save(Persistent annotation, Object parent, Field field) {
        try {
            field.setAccessible(true);
            Serializer serializer = serializers.get(annotation.serializer());
            if (serializer == null) {
                LoggerFactory.getLogger(parent.getClass()).error("the serializer is unkown: " + annotation.serializer());
                return;
            }
            Object value = field.get(parent);
            //save the referenced object instead
            if (value instanceof PersistentReference) {
                value = ((PersistentReference) value).get();
            }
            write(folder, annotation.name(), serializer, field.getType(), value);
        } catch (IllegalAccessException e) {
            throw new Panic(e);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).warn("failed to save", e);
        }
    }

    public void load(Scope scope, Persistent annotation, Object parent, Field field) {
        try {
            field.setAccessible(true);
            Serializer serializer = serializers.get(annotation.serializer());
            if (serializer == null) {
                LoggerFactory.getLogger(parent.getClass()).error("the serializer is unkown: " + annotation.serializer());
                return;
            }

            Class<?> typeToResolve = field.getType();
            boolean isReference = false;
            //check if we have a nested special case with reference - to support manual save -, so that we have the correct type
            if (field.getType() == Reference.class || field.getType() == PersistentReference.class) { //no assignable here, otherwise we would create a lot of constructur issues
                isReference = true;
                typeToResolve = getBestClassFromType(field.getGenericType());
            }


            Object resolvedValue = read(folder, annotation.name(), serializer, typeToResolve);
            //check if null and not resolvable -> try to create such an instance
            if (resolvedValue == null) {
                Container container = scope.resolve(Container.NAME_CONTAINER, Container.class);
                if (container != null) {
                    //await has danger of deadlocks, especially for PostConstructs in main thread (which is the default case)
                    resolvedValue = Async.await(container.createComponent(scope, typeToResolve)).get();
                }
            }

            if (isReference) {
                PersistentReference ref = new PersistentReference(this, scope, annotation, parent, field);
                ref.set(resolvedValue);
                field.set(parent, ref);
            } else {
                field.set(parent, resolvedValue);
            }
            LoggerFactory.getLogger(parent.getClass()).info("{}.{} = {}", parent.getClass().getSimpleName(), field.getName(), DefaultFactory.stripToString(resolvedValue));
        } catch (IllegalAccessException | IOException e) {
            LoggerFactory.getLogger(parent.getClass()).error("failed to deserialize: {}.{} -> {}", parent.getClass().getSimpleName(), field.getName(), e);
        }
    }

    private static Class<?> getBestClassFromType(Type type) {
        if (type instanceof ParameterizedType) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if (types != null && types.length > 0) {
                return (Class) types[0];
            }
        }
        if (type instanceof WildcardType) {
            return (Class) ((WildcardType) type).getUpperBounds()[0];
        }
        throw new Panic("not supported type: " + type);
    }


    public static void write(File targetDir, String name, Serializer serializer, Class<?> type, Object obj) throws IOException {
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

    public final static class PersistentReference<T> implements Reference<T> {

        private T value;
        private final Persistent annotation;
        private final HCFFieldPersistent persistent;
        private final Object parent;
        private final Field field;
        private final Scope scope;

        PersistentReference(HCFFieldPersistent persistent, Scope scope, Persistent annotation, Object parent, Field field) {
            this.annotation = annotation;
            this.persistent = persistent;
            this.parent = parent;
            this.field = field;
            this.scope = scope;
            field.setAccessible(true);
        }

        @Override
        public T get() {
            //for the barrier -> java memory model
            synchronized (this) {
                return value;
            }
        }

        @Override
        public void set(T value) {
            synchronized (this) {
                try {
                    this.value = value;
                    field.set(parent, this);
                } catch (IllegalAccessException e) {
                    throw new Panic(e);
                }
                save();
            }
        }

        /**
         * Explicitly tries to load the persisted value into this reference instance.
         * If not possible the value will be null. See also {@link #read(File, String, Serializer, Class)}
         */
        public void load() {
            synchronized (this) {
                persistent.load(scope, annotation, parent, field);
            }
        }

        /**
         * Explicitly tries to save the current value of this reference, ignoring failures.
         * See also {@link #write(File, String, Serializer, Class, Object)}
         */
        public void save() {
            synchronized (this) {
                persistent.save(annotation, parent, field);
            }
        }

        @Override
        public String toString() {
            synchronized (this) {
                if (value == null) {
                    return null;
                } else {
                    return value.toString();
                }
            }

        }
    }
}
