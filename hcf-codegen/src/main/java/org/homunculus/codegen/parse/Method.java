package org.homunculus.codegen.parse;

import org.homunculus.codegen.generator.LintException;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public interface Method {

    boolean isPrivate();

    boolean isPublic();

    boolean isAbstract();

    boolean isProtected();

    boolean isNative();

    boolean isStatic();

    default boolean isDefault() {
        return !isNative() && !isProtected() && !isPublic() && !isPrivate();
    }

    /**
     * True if this method is declared directly in the class
     */
    boolean isDeclared();

    String getName();

    /**
     * Returns all annotations, including inherited
     */
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

    void throwLintException(String msg) throws LintException;

    List<Parameter> getParameters();
}
