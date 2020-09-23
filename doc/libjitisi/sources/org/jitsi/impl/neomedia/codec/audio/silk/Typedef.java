package org.jitsi.impl.neomedia.codec.audio.silk;

public class Typedef {
    static final /* synthetic */ boolean $assertionsDisabled = (!Typedef.class.desiredAssertionStatus());
    static final boolean SKP_FALSE = false;
    static final boolean SKP_TRUE = true;
    static final short SKP_int16_MAX = Short.MAX_VALUE;
    static final short SKP_int16_MIN = Short.MIN_VALUE;
    static final int SKP_int32_MAX = Integer.MAX_VALUE;
    static final int SKP_int32_MIN = Integer.MIN_VALUE;
    static final long SKP_int64_MAX = Long.MAX_VALUE;
    static final long SKP_int64_MIN = Long.MIN_VALUE;
    static final byte SKP_int8_MAX = Byte.MAX_VALUE;
    static final byte SKP_int8_MIN = Byte.MIN_VALUE;
    static final int SKP_uint16_MAX = 65535;
    static final int SKP_uint16_MIN = 0;
    static final long SKP_uint32_MAX = 4294967295L;
    static final long SKP_uint32_MIN = 0;
    static final short SKP_uint8_MAX = (short) 255;
    static final short SKP_uint8_MIN = (short) 0;

    static int SKP_STR_CASEINSENSITIVE_COMPARE(String x, String y) {
        return x.compareTo(y);
    }

    static void SKP_assert(boolean COND) {
        if (!$assertionsDisabled && !COND) {
            throw new AssertionError();
        }
    }
}
