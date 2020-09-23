package com.lti.utils;

public final class ObjUtils {
    public static boolean equal(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    private ObjUtils() {
    }
}
