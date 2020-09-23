package org.jitsi.util;

public class OSUtils {
    public static final boolean IS_32_BIT;
    public static final boolean IS_64_BIT;
    public static final boolean IS_ANDROID;
    public static final boolean IS_FREEBSD;
    public static final boolean IS_LINUX;
    public static final boolean IS_LINUX32;
    public static final boolean IS_LINUX64;
    public static final boolean IS_MAC;
    public static final boolean IS_MAC32;
    public static final boolean IS_MAC64;
    public static final boolean IS_WINDOWS;
    public static final boolean IS_WINDOWS32;
    public static final boolean IS_WINDOWS64;
    public static final boolean IS_WINDOWS_7;
    public static final boolean IS_WINDOWS_8;
    public static final boolean IS_WINDOWS_VISTA;

    static {
        boolean z;
        boolean z2 = true;
        String osName = System.getProperty("os.name");
        if (osName == null) {
            IS_ANDROID = false;
            IS_LINUX = false;
            IS_MAC = false;
            IS_WINDOWS = false;
            IS_WINDOWS_VISTA = false;
            IS_WINDOWS_7 = false;
            IS_WINDOWS_8 = false;
            IS_FREEBSD = false;
        } else if (osName.startsWith("Linux")) {
            String javaVmName = System.getProperty("java.vm.name");
            if (javaVmName == null || !javaVmName.equalsIgnoreCase("Dalvik")) {
                IS_ANDROID = false;
                IS_LINUX = true;
            } else {
                IS_ANDROID = true;
                IS_LINUX = false;
            }
            IS_MAC = false;
            IS_WINDOWS = false;
            IS_WINDOWS_VISTA = false;
            IS_WINDOWS_7 = false;
            IS_WINDOWS_8 = false;
            IS_FREEBSD = false;
        } else if (osName.startsWith("Mac")) {
            IS_ANDROID = false;
            IS_LINUX = false;
            IS_MAC = true;
            IS_WINDOWS = false;
            IS_WINDOWS_VISTA = false;
            IS_WINDOWS_7 = false;
            IS_WINDOWS_8 = false;
            IS_FREEBSD = false;
        } else if (osName.startsWith("Windows")) {
            IS_ANDROID = false;
            IS_LINUX = false;
            IS_MAC = false;
            IS_WINDOWS = true;
            if (osName.indexOf("Vista") != -1) {
                z = true;
            } else {
                z = false;
            }
            IS_WINDOWS_VISTA = z;
            if (osName.indexOf("7") != -1) {
                z = true;
            } else {
                z = false;
            }
            IS_WINDOWS_7 = z;
            if (osName.indexOf("8") != -1) {
                z = true;
            } else {
                z = false;
            }
            IS_WINDOWS_8 = z;
            IS_FREEBSD = false;
        } else if (osName.startsWith("FreeBSD")) {
            IS_ANDROID = false;
            IS_LINUX = false;
            IS_MAC = false;
            IS_WINDOWS = false;
            IS_WINDOWS_VISTA = false;
            IS_WINDOWS_7 = false;
            IS_WINDOWS_8 = false;
            IS_FREEBSD = true;
        } else {
            IS_ANDROID = false;
            IS_LINUX = false;
            IS_MAC = false;
            IS_WINDOWS = false;
            IS_WINDOWS_VISTA = false;
            IS_WINDOWS_7 = false;
            IS_WINDOWS_8 = false;
            IS_FREEBSD = false;
        }
        String osArch = System.getProperty("sun.arch.data.model");
        if (osArch == null) {
            IS_32_BIT = true;
            IS_64_BIT = false;
        } else if (osArch.indexOf("32") != -1) {
            IS_32_BIT = true;
            IS_64_BIT = false;
        } else if (osArch.indexOf("64") != -1) {
            IS_32_BIT = false;
            IS_64_BIT = true;
        } else {
            IS_32_BIT = false;
            IS_64_BIT = false;
        }
        if (IS_LINUX && IS_32_BIT) {
            z = true;
        } else {
            z = false;
        }
        IS_LINUX32 = z;
        if (IS_LINUX && IS_64_BIT) {
            z = true;
        } else {
            z = false;
        }
        IS_LINUX64 = z;
        if (IS_MAC && IS_32_BIT) {
            z = true;
        } else {
            z = false;
        }
        IS_MAC32 = z;
        if (IS_MAC && IS_64_BIT) {
            z = true;
        } else {
            z = false;
        }
        IS_MAC64 = z;
        if (IS_WINDOWS && IS_32_BIT) {
            z = true;
        } else {
            z = false;
        }
        IS_WINDOWS32 = z;
        if (!(IS_WINDOWS && IS_64_BIT)) {
            z2 = false;
        }
        IS_WINDOWS64 = z2;
    }

    protected OSUtils() {
    }
}
