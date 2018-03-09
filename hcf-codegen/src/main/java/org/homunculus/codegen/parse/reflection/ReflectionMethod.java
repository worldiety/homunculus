package org.homunculus.codegen.parse.reflection;

import org.homunculus.codegen.generator.LintException;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Method;
import org.homunculus.codegen.parse.Parameter;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public class ReflectionMethod implements Method {

    private final java.lang.reflect.Method method;

    public ReflectionMethod(java.lang.reflect.Method method) {
        this.method = method;
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(method.getModifiers());
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(method.getModifiers());
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(method.getModifiers());
    }

    @Override
    public boolean isProtected() {
        return Modifier.isProtected(method.getModifiers());
    }

    @Override
    public boolean isNative() {
        return Modifier.isNative(method.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public boolean isDeclared() {
        //TODO this is not correct
        return true;
    }

    @Override
    public List<Annotation> getAnnotations() {
        List<Annotation> res = new ArrayList<>();
        for (java.lang.annotation.Annotation a : method.getAnnotations()) {
            res.add(new ReflectionAnnotation(a));
        }
        return res;
    }

    @Override
    public void throwLintException(String msg) throws LintException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public List<Parameter> getParameters() {
        throw new RuntimeException("not yet implemented");
    }
}
