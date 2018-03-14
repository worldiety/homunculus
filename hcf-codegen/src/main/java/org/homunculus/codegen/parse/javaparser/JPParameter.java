package org.homunculus.codegen.parse.javaparser;

import com.github.javaparser.ast.expr.AnnotationExpr;

import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Parameter;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<Annotation> getAnnotations() {
        List<Annotation> res = new ArrayList<>();
        for (AnnotationExpr a : parameter.getAnnotations()) {
            res.add(new JPAnnotation(ctx, new FullQualifiedName(ctx.src.getFullQualifiedName(a.getNameAsString())), a));
        }
        return res;
    }
}
