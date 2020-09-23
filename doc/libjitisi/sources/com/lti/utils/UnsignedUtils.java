package com.lti.utils;

public final class UnsignedUtils {
    public static final int MAX_UBYTE = 255;
    public static final long MAX_UINT = 4294967295L;
    public static final int MAX_USHORT = 65535;

    public static int uByteToInt(byte value) {
        return value >= (byte) 0 ? value : value + 256;
    }

    public static long uIntToLong(int value) {
        if (value >= 0) {
            return (long) value;
        }
        return 4294967296L + ((long) value);
    }

    public static int uShortToInt(short value) {
        return value >= (short) 0 ? value : value + (short) 0;
    }

    private UnsignedUtils() {
    }
}
