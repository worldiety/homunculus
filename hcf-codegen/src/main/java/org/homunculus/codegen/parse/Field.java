package org.homunculus.codegen.parse;

import org.homunculus.codegen.generator.LintException;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by Torben Schinke on 12.03.18.
 */

public interface Field {

    String getName();

    Type getType();

    List<Annotation> getAnnotations();

    LintException newLintException(String msg);

    FullQualifiedName getDeclaringType();

    boolean isPublic();

    boolean isProtected();

    boolean isPrivate();

    default boolean isDefault() {
        return !isPublic() && !isProtected() && !isPrivate();
    }

    @Nullable
    default Annotation getAnnotation(FullQualifiedName name) {
        for (Annotation annotation : getAnnotations()) {
            if (annotation.getFullQualifiedName().equals(name)) {
                return annotation;
            }
        }
        return null;
    }

    default String asJavadocAnchor() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDeclaringType());
        sb.append("#");
        sb.append(getName());
        return sb.toString();
    }

    @Nullable
    default Annotation getAnnotation(Class cl) {
        return getAnnotation(new FullQualifiedName(cl));
    }
}
