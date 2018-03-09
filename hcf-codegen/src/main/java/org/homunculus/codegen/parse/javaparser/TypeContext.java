package org.homunculus.codegen.parse.javaparser;

import com.github.javaparser.ast.body.TypeDeclaration;

/**
 * Created by Torben Schinke on 09.03.18.
 */
class TypeContext {
    SrcFile src;
    TypeDeclaration type;

    TypeContext(SrcFile src, TypeDeclaration type) {
        this.src = src;
        this.type = type;
    }


}
