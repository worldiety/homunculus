package org.homunculus.codegen.parse.reflection;

import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Constructor;
import org.homunculus.codegen.parse.Field;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Method;
import org.homunculus.codegen.parse.Resolver;
import org.homunculusframework.lang.Reflection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public class ReflectionResolver implements Resolver {

    @Override
    public boolean has(FullQualifiedName name) {
        try {
            Class.forName(name.toString());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public List<Constructor> getConstructors(FullQualifiedName name) throws ClassNotFoundException {
        throw new RuntimeException("not yet implemented");
    }

    @Override
    public List<Method> getMethods(FullQualifiedName name) throws ClassNotFoundException {
        Class c = null;
        c = Class.forName(name.toString());
        List<Method> res = new ArrayList<>();
        for (java.lang.reflect.Method m : Reflection.getMethods(c)) {
            res.add(new ReflectionMethod(m));
        }
        return res;
    }

    @Override
    public List<Field> getFields(FullQualifiedName name) throws ClassNotFoundException {
        Class c = null;
        c = Class.forName(name.toString());
        List<Field> res = new ArrayList<>();
        for (java.lang.reflect.Field m : Reflection.getFields(c)) {
            res.add(new ReflectionField(m));
        }
        return res;
    }

    @Override
    public List<FullQualifiedName> getTypes() {
        //TODO not correct
        return new ArrayList<>();
    }

    @Override
    public boolean isAbstract(FullQualifiedName name) {
        //TODO
        return false;
    }

    @Override
    public boolean isPublic(FullQualifiedName name) {
        //TODO
        return false;
    }

    @Override
    public boolean isPrivate(FullQualifiedName name) {
        //TODO
        return false;
    }

    @Override
    public boolean isStatic(FullQualifiedName name) {
        //TODO
        return false;
    }

    @Override
    public boolean isNested(FullQualifiedName name) {
        //TODO
        return false;
    }

    @Override
    public boolean isTopLevelType(FullQualifiedName name) {
        //TODO
        return true;
    }


    @Override
    public List<Annotation> getAnnotations(FullQualifiedName name) {
        //TODO
        return new ArrayList<>();
    }

    @Override
    public boolean isInstanceOf(FullQualifiedName which, FullQualifiedName what) {
        //TODO
        return false;
    }
}
