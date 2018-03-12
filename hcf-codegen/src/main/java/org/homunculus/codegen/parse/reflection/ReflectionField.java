package org.homunculus.codegen.parse.reflection;

import org.homunculus.codegen.generator.LintException;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Field;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Torben Schinke on 12.03.18.
 */

public class ReflectionField implements Field {

    private final java.lang.reflect.Field field;

    public ReflectionField(java.lang.reflect.Field field) {
        this.field = field;
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public Type getType() {
        //TODO this is not correct for generics
        Type type = new Type(new FullQualifiedName(field.getType()));
        return type;
    }

    @Override
    public List<Annotation> getAnnotations() {
        List<Annotation> res = new ArrayList<>();
        for (java.lang.annotation.Annotation a : field.getAnnotations()) {
            res.add(new ReflectionAnnotation(a));
        }
        return res;
    }

    @Override
    public LintException newLintException(String msg) {
        throw new RuntimeException("not yet implemented");
    }
}
