package org.homunculusframework.scope;

import org.homunculusframework.factory.scope.LifecycleOwner;
import org.homunculusframework.lang.Destroyable;
import org.homunculusframework.lang.Reference;

import java.util.IdentityHashMap;
import java.util.TreeMap;

/**
 * Similar to a {@link LifecycleLocal} but provides a named and typed key-value pair.
 * <p>
 * Created by Torben Schinke on 22.03.18.
 */

public class LifecycleEntry<T> implements Reference<T>, Destroyable {

    private final static IdentityHashMap<Class<?>, TreeMap<String, LifecycleEntry>> entries = new IdentityHashMap<>();


    private OnDestroyCallback callback;
    private T value;
    private LifecycleOwner owner;

    private LifecycleEntry(LifecycleOwner owner) {
        callback = l -> value = null;
    }

    /**
     * Gets or creates a reference whose lifecycle is bound to the given owner. If the owner is destroyed,
     * the value and this entry will be too.
     */
    public static <T> LifecycleEntry<T> get(LifecycleOwner owner, String name, Class<T> type) {
        synchronized (entries) {
            TreeMap<String, LifecycleEntry> kinds = entries.get(type);
            if (kinds == null) {
                kinds = new TreeMap<>();
                entries.put(type, kinds);
            }

            LifecycleEntry entry = kinds.get(name);
            if (entry == null) {
                entry = new LifecycleEntry(owner);
                owner.addDestroyCallback(entry.callback);
                kinds.put(name, entry);
            }
            return entry;
        }
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

    @Override
    public void destroy() {
        value = null;
        LifecycleOwner owner = this.owner;
        if (owner != null) {
            owner.removeDestroyCallback(callback);
            owner = null;
        }
    }
}
