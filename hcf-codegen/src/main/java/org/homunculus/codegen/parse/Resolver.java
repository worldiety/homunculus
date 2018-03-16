package org.homunculus.codegen.parse;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public interface Resolver {

    boolean has(FullQualifiedName name);

    List<Constructor> getConstructors(FullQualifiedName name) throws ClassNotFoundException;

    List<Method> getMethods(FullQualifiedName name) throws ClassNotFoundException;

    List<Field> getFields(FullQualifiedName name) throws ClassNotFoundException;

    /**
     * Returns all available types.
     */
    List<FullQualifiedName> getTypes();

    boolean isAbstract(FullQualifiedName name);

    boolean isPublic(FullQualifiedName name);

    boolean isPrivate(FullQualifiedName name);

    boolean isTopLevelType(FullQualifiedName name);

    default boolean isDefault(FullQualifiedName name) {
        return !isPublic(name) && !isPrivate(name);
    }

    boolean isStatic(FullQualifiedName name);

    boolean isNested(FullQualifiedName name);

    List<Annotation> getAnnotations(FullQualifiedName name);

    @Nullable
    default Annotation getAnnotation(FullQualifiedName type, FullQualifiedName annotation) {
        for (Annotation a : getAnnotations(type)) {
            if (a.getFullQualifiedName().equals(annotation)) {
                return a;
            }
        }
        return null;
    }

    @Nullable
    default Annotation getAnnotation(FullQualifiedName type, Class annotation) {
        return getAnnotation(type, new FullQualifiedName(annotation));
    }

    boolean isInstanceOf(FullQualifiedName which, FullQualifiedName what);
}
