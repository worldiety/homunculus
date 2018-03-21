package org.homunculus.codegen.parse.jcodemodel;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;

import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Constructor;
import org.homunculus.codegen.parse.Field;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Method;
import org.homunculus.codegen.parse.Resolver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Torben Schinke on 20.03.18.
 */

public class JCodeModelResolver implements Resolver {
    private final JCodeModel code;

    public JCodeModelResolver(JCodeModel code) {
        this.code = code;
    }

    @Override
    public void getSuperTypes(FullQualifiedName name, List<FullQualifiedName> dst) throws ClassNotFoundException {
        JDefinedClass jc = code._getClass(name.toString());
        if (jc == null) {
            throw new ClassNotFoundException(name.toString());
        }
        Iterator<AbstractJClass> it = jc._implements();
        while (it.hasNext()) {
            AbstractJClass a = it.next();
            getSuperTypes(new FullQualifiedName(a.fullName()), dst);
        }

        while (jc._extends() != null) {
            AbstractJClass a = jc._extends();
            getSuperTypes(new FullQualifiedName(a.fullName()), dst);
        }
    }

    public void listTypes(FullQualifiedName src, List<FullQualifiedName> found, List<FullQualifiedName> notFound) {
        JDefinedClass jc = code._getClass(src.toString());
        if (jc == null) {
            notFound.add(src);
            return;
        }
        found.add(src);
        Iterator<AbstractJClass> it = jc._implements();
        while (it.hasNext()) {
            AbstractJClass a = it.next();
            listTypes(new FullQualifiedName(a.fullName()), found, notFound);
        }

        if (jc._extends() != null) {
            AbstractJClass a = jc._extends();
            listTypes(new FullQualifiedName(a.fullName()), found, notFound);
        }
    }

    @Override
    public boolean has(FullQualifiedName name) {
        return code._getClass(name.toString()) != null;
    }

    @Override
    public List<Constructor> getConstructors(FullQualifiedName name) throws ClassNotFoundException {
        JDefinedClass cl = code._getClass(name.toString());
        if (cl == null) {
            throw new ClassNotFoundException(name.toString());
        }
        List<Constructor> res = new ArrayList<>();
        Iterator<JMethod> it = cl.constructors();
        while (it.hasNext()) {
            JMethod m = it.next();
            res.add(new JCodeModelConstructor(m));
        }
        return res;
    }

    @Override
    public List<Method> getMethods(FullQualifiedName name) throws ClassNotFoundException {
        return null;
    }

    @Override
    public List<Field> getFields(FullQualifiedName name) throws ClassNotFoundException {
        return null;
    }

    @Override
    public List<FullQualifiedName> getTypes() {
        return null;
    }

    @Override
    public boolean isAbstract(FullQualifiedName name) {
        return false;
    }

    @Override
    public boolean isPublic(FullQualifiedName name) {
        return false;
    }

    @Override
    public boolean isPrivate(FullQualifiedName name) {
        return false;
    }

    @Override
    public boolean isTopLevelType(FullQualifiedName name) {
        return false;
    }

    @Override
    public boolean isStatic(FullQualifiedName name) {
        return false;
    }

    @Override
    public boolean isNested(FullQualifiedName name) {
        return false;
    }

    @Override
    public List<Annotation> getAnnotations(FullQualifiedName name) {
        return null;
    }

    @Override
    public boolean isInstanceOf(FullQualifiedName which, FullQualifiedName what) {
        return false;
    }
}
