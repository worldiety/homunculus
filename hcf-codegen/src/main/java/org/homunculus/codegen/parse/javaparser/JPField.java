package org.homunculus.codegen.parse.javaparser;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import org.homunculus.codegen.generator.LintException;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Field;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Torben Schinke on 12.03.18.
 */

public class JPField implements Field {

    private final FieldDeclaration fieldDeclaration;
    private final TypeContext ctx;
    private final boolean isDeclared;

    public JPField(TypeContext ctx, FieldDeclaration fieldDeclaration, boolean isDeclared) {
        this.fieldDeclaration = fieldDeclaration;
        this.ctx = ctx;
        this.isDeclared = isDeclared;
    }

    @Override
    public FullQualifiedName getDeclaringType() {
        return ctx.name;
    }

    @Override
    public boolean isPublic() {
        return fieldDeclaration.isPublic();
    }

    @Override
    public boolean isProtected() {
        return fieldDeclaration.isProtected();
    }

    @Override
    public boolean isPrivate() {
        return fieldDeclaration.isPrivate();
    }

    @Override
    public String getName() {
        return fieldDeclaration.getVariables().get(0).getNameAsString();
    }

    @Override
    public Type getType() {
        return TypeUtil.convert(ctx, fieldDeclaration.getVariables().get(0).getType());
    }


    @Override
    public List<Annotation> getAnnotations() {
        List<Annotation> res = new ArrayList<>();
        for (AnnotationExpr a : fieldDeclaration.getAnnotations()) {
            res.add(new JPAnnotation(ctx, new FullQualifiedName(ctx.src.getFullQualifiedName(a.getNameAsString())), a));
        }
        return res;
    }

    @Override
    public LintException newLintException(String msg) {
        throw new LintException(msg, ctx.src, fieldDeclaration.getRange().get());
    }


}
