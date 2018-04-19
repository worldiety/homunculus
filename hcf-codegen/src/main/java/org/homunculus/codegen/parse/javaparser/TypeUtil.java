package org.homunculus.codegen.parse.javaparser;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import org.homunculus.codegen.parse.FullQualifiedName;
import org.homunculus.codegen.parse.Type;

/**
 * Created by Torben Schinke on 14.03.18.
 */

class TypeUtil {

    private TypeUtil() {

    }

    static Type convert(TypeContext ctx, com.github.javaparser.ast.type.Type parserType) {
        if (parserType instanceof ClassOrInterfaceType) {
            //TODO this is not correct for generics
            ClassOrInterfaceType cit = (ClassOrInterfaceType) parserType;

            //when the name clashes, java has a full-qualified name instead (TODO not true for partial dot-imports)
            StringBuilder str = new StringBuilder();
            cit.getScope().ifPresent(s -> str.append(s.asString()).append("."));
            str.append(cit.getName().asString());

            return new Type(new FullQualifiedName(ctx.src.getFullQualifiedName(str.toString())));
        }
        return new Type(new FullQualifiedName(ctx.src.getFullQualifiedName(parserType.toString())));
    }

}
