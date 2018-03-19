package org.homunculus.codegen.parse.reflection;

import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aerlemann on 19.03.18.
 */

public class ReflectionParameter implements Parameter {

    private final java.lang.reflect.Parameter p;

    public ReflectionParameter(java.lang.reflect.Parameter p) {
        this.p = p;
    }

    @Override
    public String getName() {
        return p.getName();
    }

    @Override
    public FullQualifiedName getType() {
        return new FullQualifiedName(p.getType());
    }

    @Override
    public List<Annotation> getAnnotations() {
        List<Annotation> res = new ArrayList<>();
        for (java.lang.annotation.Annotation a:p.getAnnotations()){
            res.add(new ReflectionAnnotation(a));
        }
        return res;
    }
}
