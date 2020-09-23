package com.lti.utils;

public final class PathUtils {
    public static String extractExtension(String s) {
        int index = s.lastIndexOf(".");
        if (index < 0) {
            return "";
        }
        return s.substring(index + 1, s.length());
    }

    public static String getTempPath() {
        return System.getProperty("java.io.tmpdir");
    }

    private PathUtils() {
    }
}
