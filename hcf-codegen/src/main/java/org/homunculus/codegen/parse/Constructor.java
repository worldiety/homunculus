package org.homunculus.codegen.parse;

import org.homunculus.codegen.generator.LintException;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public interface Constructor {

    boolean isPrivate();

    boolean isPublic();

    boolean isAbstract();

    boolean isProtected();


    boolean isStatic();

    default boolean isDefault() {
        return !isProtected() && !isPublic() && !isPrivate();
    }

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


    FullQualifiedName getDeclaringType();


    default String asJavadocAnchor() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDeclaringType());
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
