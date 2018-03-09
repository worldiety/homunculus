package org.homunculus.codegen.parse;

import java.util.List;

/**
 * Created by Torben Schinke on 09.03.18.
 */

public interface Resolver {

    List<Method> getMethods(FullQualifiedName name) throws ClassNotFoundException;

}
