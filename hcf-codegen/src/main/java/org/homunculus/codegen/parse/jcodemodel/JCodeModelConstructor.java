package org.homunculus.codegen.parse.jcodemodel;

import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.generator.LintException;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Constructor;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Torben Schinke on 21.03.18.
 */

public class JCodeModelConstructor implements Constructor {

    private final JMethod method;

    public JCodeModelConstructor(JMethod method) {
        this.method = method;
    }

    @Override
    public boolean isPrivate() {
        return false;
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean isAbstract() {
        return false;
    }

    @Override
    public boolean isProtected() {
        return false;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public List<Annotation> getAnnotations() {
        return null;
    }

    @Override
    public LintException newLintException(String msg) {
        return null;
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> res = new ArrayList<>();
        for (JVar var : method.params()) {
            res.add(new JCodeModelParameter(var));
        }
        return res;
    }

    @Override
    public FullQualifiedName getDeclaringType() {
        return null;
    }
}
