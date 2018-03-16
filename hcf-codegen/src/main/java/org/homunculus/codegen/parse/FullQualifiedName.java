package org.homunculus.codegen.parse;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public class FullQualifiedName implements Comparable<FullQualifiedName> {
    private final String fullqualifiedName;

    public FullQualifiedName(String fullqualifiedName) {
        this.fullqualifiedName = fullqualifiedName;
    }

    public FullQualifiedName(Class c) {
        this(c.getName());
    }

    /**
     * E.g. my.package.MyClass returns MyClass
     */
    public String getSimpleName() {
        int idx = fullqualifiedName.lastIndexOf('.');
        if (idx < 0) {
            return fullqualifiedName;
        }
        return fullqualifiedName.substring(idx + 1);
    }

    /**
     * E.g. my.package.MyClass returns my.package
     */
    public String getPackageName() {
        int idx = fullqualifiedName.lastIndexOf('.');
        if (idx < 0) {
            return fullqualifiedName;
        }
        return fullqualifiedName.substring(0, idx);
    }

    /**
     * E.g. my.package.MyClass returns my.package.MyClass
     */
    @Override
    public String toString() {
        return fullqualifiedName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FullQualifiedName that = (FullQualifiedName) o;

        return fullqualifiedName.equals(that.fullqualifiedName);
    }

    @Override
    public int hashCode() {
        return fullqualifiedName.hashCode();
    }

    @Override
    public int compareTo(@NotNull FullQualifiedName o) {
        return this.compareTo(o);
    }
}
