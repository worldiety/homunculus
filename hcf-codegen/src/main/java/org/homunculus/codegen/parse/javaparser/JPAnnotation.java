package org.homunculus.codegen.parse.javaparser;

import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;

import org.homunculus.codegen.generator.LintException;
import org.homunculus.codegen.parse.Annotation;
import org.homunculus.codegen.parse.FullQualifiedName;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;


/**
 * Created by Torben Schinke on 09.03.18.
 */

public class JPAnnotation implements Annotation {

    private final AnnotationExpr annotationDeclaration;
    private final FullQualifiedName fqn;
    private final TypeContext ctx;

    public JPAnnotation(TypeContext ctx, FullQualifiedName fqn, AnnotationExpr annotationDeclaration) {
        this.annotationDeclaration = annotationDeclaration;
        this.fqn = fqn;
        this.ctx = ctx;

    }

    @Override
    public FullQualifiedName getFullQualifiedName() {
        return fqn;
    }

    @Nullable
    @Override
    public String getString(String key) {
        if (key == null || key.isEmpty()) {
            key = "value";
        }
        if (annotationDeclaration instanceof SingleMemberAnnotationExpr && key.equals("value")) {
            SingleMemberAnnotationExpr expr = ((SingleMemberAnnotationExpr) annotationDeclaration);
            if (expr.getMemberValue() instanceof StringLiteralExpr) {
                return ((StringLiteralExpr) expr.getMemberValue()).getValue();
            } else if (expr.getMemberValue() instanceof NameExpr) {
                NameExpr nameExpr = ((NameExpr) expr.getMemberValue());
                String value = resolveLocalStaticFieldValue(ctx.src, nameExpr.getNameAsString());
                if (value != null) {
                    return value;
                }

                LoggerFactory.getLogger(getClass()).warn("constant evaluation from NameExpr not supported: {}", nameExpr);
                return null;
            } else if (expr.getMemberValue() instanceof IntegerLiteralExpr) {
                IntegerLiteralExpr integerExp = (IntegerLiteralExpr) expr.getMemberValue();
                return Integer.toString(integerExp.asInt());
            } else if (expr.getMemberValue() instanceof FieldAccessExpr) {
                FieldAccessExpr fieldExpr = (FieldAccessExpr) expr.getMemberValue();
                String scopeName = ((NameExpr) fieldExpr.getScope()).getNameAsString();
                FullQualifiedName fqnScope = new FullQualifiedName(ctx.src.getFullQualifiedName(scopeName));
                //currently just supporting from the reflection value
                try {
                    return (String) Class.forName(fqnScope.toString()).getField(fieldExpr.getNameAsString()).get(null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (expr.getMemberValue() instanceof UnaryExpr) {
                UnaryExpr fieldExpr = (UnaryExpr) expr.getMemberValue();
                return fieldExpr.getOperator().asString() + fieldExpr.getExpression().toString();
            } else {
                LoggerFactory.getLogger(getClass()).warn("constant evaluation from SingleMemberAnnotationExpr.member not supported: {} -> {}", expr.getMemberValue().getClass(), expr);
                return null;
            }
        }
        LoggerFactory.getLogger(getClass()).warn("constant evaluation from annotationDeclaration not supported: {}", annotationDeclaration.getClass());

        return null;
    }


    @Nullable
    private static String resolveLocalStaticFieldValue(SrcFile src, String fieldName) {
        for (FieldDeclaration f : src.getUnit().getClassByName(src.getPrimaryClassName()).get().getFields()) {
            if (f.getVariables().size() > 0) {
                VariableDeclarator var = f.getVariables().get(0);
                if (var.getNameAsString().equals(fieldName)) {
                    if (var.getInitializer().isPresent()) {
                        if (var.getInitializer().get() instanceof StringLiteralExpr) {
                            return var.getInitializer().get().asStringLiteralExpr().asString();
                        }

                    }
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getFullQualifiedName().toString();
    }

    @Nullable
    @Override
    public FullQualifiedName getConstant(String key) {
        if (annotationDeclaration instanceof SingleMemberAnnotationExpr) {
            SingleMemberAnnotationExpr expr = ((SingleMemberAnnotationExpr) annotationDeclaration);
            if (expr.getMemberValue() instanceof FieldAccessExpr) {
                FieldAccessExpr fae = (FieldAccessExpr) expr.getMemberValue();
                //this is something like R.string
                String tmp = fae.getScope().toString();
                int fIdxDot = tmp.indexOf('.');
                if (fIdxDot > 0) {
                    tmp = ctx.src.getFullQualifiedName(tmp.substring(0, fIdxDot)) + tmp.substring(fIdxDot);
                }
                return new FullQualifiedName(tmp + "." + fae.getNameAsString());
            }
        }
        return null;
    }

    @Override
    public LintException newLintException(String msg) {
        return new LintException(msg, ctx.src, annotationDeclaration.getRange().get());
    }
}
