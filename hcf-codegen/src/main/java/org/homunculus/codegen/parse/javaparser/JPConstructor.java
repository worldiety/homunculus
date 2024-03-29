package org.homunculus.codegen.parse.javaparser;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import org.homunculus.codegen.generator.LintException;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.Constructor;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public class JPConstructor implements Constructor {
    private final ConstructorDeclaration methodDeclaration;
    private final TypeContext ctx;
    private final FullQualifiedName declaringType;

    public JPConstructor(TypeContext ctx, FullQualifiedName declaringType, ConstructorDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
        this.ctx = ctx;
        this.declaringType = declaringType;
    }

    @Override
    public boolean isPrivate() {
        return methodDeclaration.isPrivate();
    }

    @Override
    public boolean isPublic() {
        return methodDeclaration.isPublic();
    }

    @Override
    public boolean isAbstract() {
        return methodDeclaration.isAbstract();
    }

    @Override
    public boolean isProtected() {
        return methodDeclaration.isProtected();
    }


    @Override
    public boolean isStatic() {
        return methodDeclaration.isStatic();
    }

    @Override
    public String getName() {
        return methodDeclaration.getNameAsString();
    }


    @Override
    public List<Annotation> getAnnotations() {
        //TODO this does not work with inheritation and not with reflection resolver
        List<Annotation> res = new ArrayList<>();
        for (AnnotationExpr a : methodDeclaration.getAnnotations()) {
            res.add(new JPAnnotation(ctx, new FullQualifiedName(ctx.src.getFullQualifiedName(a.getNameAsString())), a));
        }
        return res;
    }

    @Override
    public LintException newLintException(String msg) {
        return new LintException(msg, ctx.src, methodDeclaration.getRange().get());
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> res = new ArrayList<>();
        for (com.github.javaparser.ast.body.Parameter p : methodDeclaration.getParameters()) {
            res.add(new JPParameter(ctx, p));
        }
        return res;
    }


    @Override
    public FullQualifiedName getDeclaringType() {
        return declaringType;
    }
}
