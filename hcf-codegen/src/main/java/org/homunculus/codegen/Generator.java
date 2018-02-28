package org.homunculus.codegen;

/**
 * Created by Torben Schinke on 20.02.18.
 */

public interface Generator {
    void generate(GenProject project) throws Exception;
}
