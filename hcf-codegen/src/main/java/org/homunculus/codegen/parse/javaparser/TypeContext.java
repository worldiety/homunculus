package org.homunculus.codegen.parse.javaparser;

import com.github.javaparser.ast.body.TypeDeclaration;

import org.homunculus.codegen.parse.FullQualifiedName;

/**
 * Created by Torben Schinke on 09.03.18.
 */
class TypeContext {
    SrcFile src;
    TypeDeclaration type;
    FullQualifiedName name;

    TypeContext(SrcFile src, TypeDeclaration type, FullQualifiedName name) {
        this.src = src;
        this.type = type;
        this.name = name;
    }


}
