package dev.ebullient.pockets.db;

import java.util.Map;

public class Util {
    private Util() {
    }

    public static boolean updateFieldWidth(Map<String, Integer> fieldWidths, String key, Object o) {
        int prev = fieldWidths.get(key);
        int next = Math.max(prev, objToWidth(o));
        if (prev != next) {
            fieldWidths.put(key, next);
            return true;
        }
        return false;
    }

    static int objToWidth(Object o) {
        return o == null ? 1 : o.toString().length() + 1;
    }
}
