package org.homunculus.codegen.parse;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public interface Parameter {
    String getName();

    FullQualifiedName getType();

    List<Annotation> getAnnotations();

    @Nullable
    default Annotation getAnnotation(FullQualifiedName name) {
        for (Annotation annotation : getAnnotations()) {
            if (annotation.getFullQualifiedName().equals(name)) {
                return annotation;
            }
        }
        return null;
    }

    @Nullable
    default Annotation getAnnotation(Class cl) {
        return getAnnotation(new FullQualifiedName(cl));
    }
}
