package org.homunculus.codegen.parse.reflection;

import org.homunculus.codegen.generator.LintException;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.FullQualifiedName;

import javax.annotation.Nullable;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public class ReflectionAnnotation implements Annotation {

    private final java.lang.annotation.Annotation annotation;

    public ReflectionAnnotation(java.lang.annotation.Annotation annotation) {
        this.annotation = annotation;
    }

    @Override
    public FullQualifiedName getFullQualifiedName() {
        return new FullQualifiedName(annotation.getClass().getName());
    }

    @Nullable
    @Override
    public String getString(String key) {
        throw new RuntimeException("not yet implemented");
    }

    @Nullable
    @Override
    public FullQualifiedName getConstant(String key) {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public LintException newLintException(String msg) {
        throw new RuntimeException("not yet implemented");
    }
}
