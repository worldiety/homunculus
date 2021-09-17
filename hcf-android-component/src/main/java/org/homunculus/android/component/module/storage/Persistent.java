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
package org.homunculus.android.component.module.storage;

import android.content.Context;

import org.jetbrains.annotations.Nullable;

import org.homunculusframework.factory.serializer.Serializable;
import org.homunculusframework.factory.serializer.Serializer;
import org.homunculusframework.lang.Destroyable;
import org.homunculusframework.lang.Panic;
import org.homunculusframework.lang.Reference;
import org.homunculusframework.lang.Reflection;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * A simple persistent wrapper to easily serialize objects. This is more or less an anti pattern and should be
 * generally only used for prototyping or for unimportant parameters or flags. However, writing a file is cumbersome
 * and you can do a lot wrong in general, so this implementation does at least some basic house keeping.
 *
 * @author Torben Schinke
 * @since 1.0
 */
public class Persistent<T> implements Reference<T>, Destroyable {

    private final File targetDir;
    private final String name;
    private final Serializer serializer;
    private volatile T value;

    public Persistent(File targetDir, String name, Serializer serializer) {
        this.serializer = serializer;
        this.targetDir = targetDir;
        this.name = name;
        try {
            load();
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error("failed to load", e);
        }
    }

    /**
     * Writes into a "persistent" folder in the app's private folder using the default java serializable pattern.
     * Don't forget to choose a unique name.
     *
     * @param context
     * @param name
     */
    public Persistent(Context context, String name) {
        this(new File(context.getFilesDir(), "persistent"), name, new Serializable());
    }

    @Override
    @Nullable
    public T get() {
        return value;
    }

    @Override
    public void set(@Nullable T value) {
        this.value = value;
    }

    /**
     * Explicitly tries to load the persisted value into this reference instance.
     * If not possible the value will be null. See also {@link #read(File, String, Serializer, Class)}
     */
    public void load() throws IOException {
        synchronized (this) {
            this.value = (T) read(targetDir, name, serializer, Object.class);
        }
    }

    /**
     * Explicitly tries to save the current value of this reference, ignoring failures.
     * See also {@link #write(File, String, Serializer, Class, Object)}
     */
    public void save() throws IOException {
        synchronized (this) {
            write(targetDir, name, serializer, Object.class, value);
        }
    }

    /**
     * Saves the value, whatever that is, ignoring any failure.
     */
    @Override
    public void destroy() {
        try {
            save();
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("failed to save", e);
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
}
