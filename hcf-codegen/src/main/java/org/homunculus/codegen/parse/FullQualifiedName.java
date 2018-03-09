package org.homunculus.codegen.parse;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public class FullQualifiedName {
    private final String fullqualifiedName;

    public FullQualifiedName(String fullqualifiedName) {
        this.fullqualifiedName = fullqualifiedName;
    }

    public FullQualifiedName(Class c) {
        this(c.getName());
    }

    public String getSimpleName() {
        int idx = fullqualifiedName.lastIndexOf('.');
        if (idx < 0) {
            return fullqualifiedName;
        }
        return fullqualifiedName.substring(idx);
    }

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
}
