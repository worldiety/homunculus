package org.homunculus.codegen.parse;

/**
 * Created by Torben Schinke on 12.03.18.
 */

public class Strings {
    private Strings() {

    }

    public static String startUpperCase(String str) {
        String r = str.substring(0, 1).toUpperCase();
        return r + str.substring(1);
    }

    public static String startLowerCase(String str) {
        String r = str.substring(0, 1).toLowerCase();
        return r + str.substring(1);
    }
}
