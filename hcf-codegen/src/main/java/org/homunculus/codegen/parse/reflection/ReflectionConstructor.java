package org.homunculus.codegen.parse.reflection;

import org.homunculus.codegen.generator.LintException;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Constructor;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Parameter;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aerlemann on 19.03.18.
 */

public class ReflectionConstructor implements Constructor {
    private final java.lang.reflect.Constructor cons;
    private final FullQualifiedName parent;

    public ReflectionConstructor(java.lang.reflect.Constructor cons, FullQualifiedName parent) {
        this.cons = cons;
        this.parent = parent;
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(cons.getModifiers());
    }

    @Override
    public boolean isPublic() {
        return Modifier.isPublic(cons.getModifiers());
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(cons.getModifiers());
    }

    @Override
    public boolean isProtected() {
        return Modifier.isProtected(cons.getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(cons.getModifiers());
    }

    @Override
    public String getName() {
        return cons.getName();
    }

    @Override
    public List<Annotation> getAnnotations() {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public LintException newLintException(String msg) {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> res = new ArrayList<>();
        for (java.lang.reflect.Parameter p:cons.getParameters()){
            res.add(new ReflectionParameter(p));
        }
        return res;
    }

    @Override
    public FullQualifiedName getDeclaringType() {
        return parent;
    }
}
