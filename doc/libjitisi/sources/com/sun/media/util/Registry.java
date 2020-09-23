package com.sun.media.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Registry {
    private static final Map<String, Object> hash = new HashMap();

    public static boolean commit() throws IOException {
        return false;
    }

    public static Object get(String key) {
        return key == null ? null : hash.get(key);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        Object value = get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value.toString());
    }

    public static int getInt(String key, int defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean set(String key, Object value) {
        if (key == null || value == null) {
            return false;
        }
        hash.put(key, value);
        return true;
    }
}
