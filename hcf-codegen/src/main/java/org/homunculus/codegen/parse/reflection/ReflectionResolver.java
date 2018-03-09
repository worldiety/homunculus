package org.homunculus.codegen.parse.reflection;

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
    public List<Method> getMethods(FullQualifiedName name) throws ClassNotFoundException {
        Class c = null;
        c = Class.forName(name.toString());
        List<Method> res = new ArrayList<>();
        for (java.lang.reflect.Method m : Reflection.getMethods(c)) {
            res.add(new ReflectionMethod(m));
        }
        return res;
    }
}
