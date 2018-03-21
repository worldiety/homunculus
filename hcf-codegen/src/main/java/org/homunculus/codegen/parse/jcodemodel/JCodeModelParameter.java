package org.homunculus.codegen.parse.jcodemodel;

import com.helger.jcodemodel.JVar;

import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Parameter;

import java.util.List;

/**
 * Created by Torben Schinke on 21.03.18.
 */

public class JCodeModelParameter implements Parameter {

    private final JVar var;

    public JCodeModelParameter(JVar var) {
        this.var = var;
    }

    @Override
    public String getName() {
        return var.name();
    }

    @Override
    public FullQualifiedName getType() {
        return new FullQualifiedName(var.type().fullName());
    }

    @Override
    public List<Annotation> getAnnotations() {
        return null;
    }
}
