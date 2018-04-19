package org.homunculus.codegen.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Created by Torben Schinke on 12.03.18.
 */

public class Strings {
    private static final Map<String, String> QUOTED_WORDS = new HashMap<>();

    static {
        QUOTED_WORDS.put(Pattern.quote("view"), "View");
        QUOTED_WORDS.put(Pattern.quote("button"), "Button");
        QUOTED_WORDS.put(Pattern.quote("list"), "List");
        QUOTED_WORDS.put(Pattern.quote("item"), "Item");
        QUOTED_WORDS.put(Pattern.quote("entry"), "Entry");
        QUOTED_WORDS.put(Pattern.quote("table"), "Table");
    }

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

    public static String nicefy(String text) {
        for (Entry<String, String> entry : QUOTED_WORDS.entrySet()) {
            text = text.replaceFirst(entry.getKey(), entry.getValue());
        }
        return text;
    }
}
