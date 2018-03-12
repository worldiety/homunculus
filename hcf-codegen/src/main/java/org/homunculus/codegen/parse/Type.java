package org.homunculus.codegen.parse;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Torben Schinke on 12.03.18.
 */

public class Type {

    private final FullQualifiedName fullQualifiedName;
    private final List<Type> generics;

    public Type(FullQualifiedName fullQualifiedName, List<Type> generics) {
        this.fullQualifiedName = fullQualifiedName;
        this.generics = generics;
    }

    public Type(FullQualifiedName fullQualifiedName) {
        this(fullQualifiedName, new ArrayList<>());
    }

    public FullQualifiedName getFullQualifiedName() {
        return fullQualifiedName;
    }

    public List<Type> getGenerics() {
        return generics;
    }
}
