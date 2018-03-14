package org.homunculus.codegen.parse.javaparser;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

import org.homunculus.codegen.generator.LintException;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Method;
import org.homunculus.codegen.parse.Parameter;
import org.homunculus.codegen.parse.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public class JPMethod implements Method {
    private final MethodDeclaration methodDeclaration;
    private final TypeContext ctx;
    private final boolean isDeclared;
    private final FullQualifiedName declaringType;

    public JPMethod(TypeContext ctx, FullQualifiedName declaringType, MethodDeclaration methodDeclaration, boolean isDeclared) {
        this.methodDeclaration = methodDeclaration;
        this.ctx = ctx;
        this.isDeclared = isDeclared;
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
    public boolean isNative() {
        return methodDeclaration.isNative();
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
    public boolean isDeclared() {
        return isDeclared;
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
    public Type getType() {
        //TODO this is not correct for generics
        Type type = new Type(new FullQualifiedName(ctx.src.getFullQualifiedName(methodDeclaration.getType().asString())));
        return type;
    }

    @Override
    public FullQualifiedName getDeclaringType() {
        return declaringType;
    }
}
