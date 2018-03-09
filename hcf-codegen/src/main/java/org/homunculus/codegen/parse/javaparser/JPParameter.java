package org.homunculus.codegen.parse.javaparser;

import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Parameter;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public class JPParameter implements Parameter {

    private final com.github.javaparser.ast.body.Parameter parameter;
    private final TypeContext ctx;

    public JPParameter(TypeContext ctx, com.github.javaparser.ast.body.Parameter parameter) {
        this.parameter = parameter;
        this.ctx = ctx;
    }


    @Override
    public String getName() {
        return parameter.getNameAsString();
    }

    @Override
    public FullQualifiedName getType() {
        return new FullQualifiedName(ctx.src.getFullQualifiedName(parameter.getType()));
    }
}
