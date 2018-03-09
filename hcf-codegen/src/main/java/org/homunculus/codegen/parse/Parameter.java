package org.homunculus.codegen.parse;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public interface Parameter {
    String getName();

    FullQualifiedName getType();
}
