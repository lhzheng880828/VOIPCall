package net.sf.fmj.utility;

import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import org.jitsi.android.util.java.awt.Dimension;

public class FormatUtils {
    public static final Class<?> audioFormatClass = AudioFormat.class;
    public static final Class<?> byteArray = byte[].class;
    public static final Class<?> formatArray = Format[].class;
    public static final Class<?> intArray = int[].class;
    public static final Class<?> shortArray = short[].class;
    public static final Class<?> videoFormatClass = VideoFormat.class;

    public static boolean byteArraysEqual(byte[] ba1, byte[] ba2) {
        if (ba1 == null && ba2 == null) {
            return true;
        }
        if (ba1 == null || ba2 == null) {
            return false;
        }
        if (ba1.length != ba2.length) {
            return false;
        }
        for (int i = 0; i < ba1.length; i++) {
            if (ba1[i] != ba2[i]) {
                return false;
            }
        }
        return true;
    }

    private static int charEncodingCodeVal(char c) {
        if (c <= '_') {
            return c - 32;
        }
        if (c == '`') {
            return -1;
        }
        if (c <= 'z') {
            return c - 64;
        }
        if (c <= 127) {
            return -1;
        }
        if (c <= 191) {
            return -94;
        }
        if (c <= 255) {
            return -93;
        }
        return -1;
    }

    public static Dimension clone(Dimension d) {
        if (d == null) {
            return null;
        }
        return new Dimension(d);
    }

    public static String frameRateToString(float frameRate) {
        return "" + (((float) ((long) (frameRate * 10.0f))) / 10.0f);
    }

    public static boolean isOneAssignableFromTheOther(Class<?> a, Class<?> b) {
        return a == b || b.isAssignableFrom(a) || a.isAssignableFrom(b);
    }

    public static boolean isSubclass(Class<?> a, Class<?> b) {
        if (a != b && b.isAssignableFrom(a)) {
            return true;
        }
        return false;
    }

    public static boolean matches(double v1, double v2) {
        if (v1 == -1.0d || v2 == -1.0d || v1 == v2) {
            return true;
        }
        return false;
    }

    public static boolean matches(float v1, float v2) {
        if (v1 == -1.0f || v2 == -1.0f || v1 == v2) {
            return true;
        }
        return false;
    }

    public static boolean matches(int v1, int v2) {
        if (v1 == -1 || v2 == -1 || v1 == v2) {
            return true;
        }
        return false;
    }

    public static boolean matches(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return true;
        }
        return o1.equals(o2);
    }

    public static boolean nullSafeEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    public static boolean nullSafeEqualsIgnoreCase(String o1, String o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equalsIgnoreCase(o2);
    }

    public static boolean specified(double v) {
        return v != -1.0d;
    }

    public static boolean specified(float v) {
        return v != -1.0f;
    }

    public static boolean specified(int v) {
        return v != -1;
    }

    public static boolean specified(Object o) {
        return o != null;
    }

    public static long stringEncodingCodeVal(String s) {
        long result = 0;
        for (int i = 0; i < s.length(); i++) {
            result = (result * 64) + ((long) charEncodingCodeVal(s.charAt(i)));
        }
        return result;
    }
}
