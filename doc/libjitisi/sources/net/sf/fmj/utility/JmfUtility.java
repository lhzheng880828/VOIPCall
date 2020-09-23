package net.sf.fmj.utility;

public class JmfUtility {
    public static boolean enableLogging() {
        try {
            Class.forName("com.sun.media.util.Registry").getMethod("set", new Class[]{String.class, Object.class}).invoke(null, new Object[]{"allowLogging", Boolean.valueOf(true)});
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
