package org.homunculus.codegen.parse;

import org.homunculus.codegen.generator.LintException;

import javax.annotation.Nullable;


/**
 * Created by Torben Schinke on 09.03.18.
 */

public interface Annotation {

    FullQualifiedName getFullQualifiedName();

    /**
     * Tries to parse a key from the annotation as string constant
     *
     * @param key the name, default is "value"
     * @return null if available and somehow castable into a string
     */
    @Nullable
    String getString(String key);

    @Nullable
    default Long getLong(String key) {
        try {
            String v = getString(key);
            if (v == null) {
                return null;
            }
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            //intentionally ignored
        }
        return null;
    }

    /**
     * Tries to return the constant expression
     *
     * @param key
     * @return
     */
    @Nullable
    FullQualifiedName getConstant(String key);

    LintException newLintException(String msg);
}
