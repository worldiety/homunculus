package org.homunculus.codegen.parse;

import org.homunculus.codegen.generator.LintException;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public interface Method {

    boolean isPrivate();

    boolean isPublic();

    boolean isAbstract();

    boolean isProtected();

    boolean isNative();

    boolean isStatic();

    default boolean isDefault() {
        return !isNative() && !isProtected() && !isPublic() && !isPrivate();
    }

    /**
     * True if this method is declared directly in the class
     */
    boolean isDeclared();

    String getName();

    /**
     * Returns all annotations, including inherited
     */
    List<Annotation> getAnnotations();

    @Nullable
    default Annotation getAnnotation(FullQualifiedName name) {
        for (Annotation annotation : getAnnotations()) {
            if (annotation.getFullQualifiedName().equals(name)) {
                return annotation;
            }
        }
        return null;
    }

    @Nullable
    default Annotation getAnnotation(Class cl) {
        return getAnnotation(new FullQualifiedName(cl));
    }

    LintException newLintException(String msg);

    List<Parameter> getParameters();

    Type getType();

    FullQualifiedName getDeclaringType();


    default String asJavadocAnchor() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDeclaringType()).append("#");
        sb.append(getName());
        sb.append("(");
        for (Parameter p : getParameters()) {
            sb.append(p.getType());
            sb.append(",");
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }
        sb.append(")");
        return sb.toString();
    }
}
