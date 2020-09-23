package org.jitsi.impl.neomedia.codec.audio.silk;

public class Macros {
    static int SKP_SMULWB(int a32, int b32) {
        return ((a32 >> 16) * ((short) b32)) + (((65535 & a32) * ((short) b32)) >> 16);
    }

    static int SKP_SMLAWB(int a32, int b32, int c32) {
        return (((b32 >> 16) * ((short) c32)) + (((65535 & b32) * ((short) c32)) >> 16)) + a32;
    }

    static int SKP_SMULWT(int a32, int b32) {
        return ((a32 >> 16) * (b32 >> 16)) + (((65535 & a32) * (b32 >> 16)) >> 16);
    }

    static int SKP_SMLAWT(int a32, int b32, int c32) {
        return (((b32 >> 16) * (c32 >> 16)) + a32) + (((65535 & b32) * (c32 >> 16)) >> 16);
    }

    static int SKP_SMULBB(int a32, int b32) {
        return ((short) a32) * ((short) b32);
    }

    static int SKP_SMLABB(int a32, int b32, int c32) {
        return (((short) b32) * ((short) c32)) + a32;
    }

    static int SKP_SMULBT(int a32, int b32) {
        return ((short) a32) * (b32 >> 16);
    }

    static int SKP_SMLABT(int a32, int b32, int c32) {
        return (((short) b32) * (c32 >> 16)) + a32;
    }

    static long SKP_SMLAL(long a64, int b32, int c32) {
        return (((long) b32) * ((long) c32)) + a64;
    }

    static int SKP_SMULWW(int a32, int b32) {
        return SKP_SMULWB(a32, b32) + (SigProcFIX.SKP_RSHIFT_ROUND(b32, 16) * a32);
    }

    static int SKP_SMLAWW(int a32, int b32, int c32) {
        return SKP_SMLAWB(a32, b32, c32) + (SigProcFIX.SKP_RSHIFT_ROUND(c32, 16) * b32);
    }

    static int SKP_ADD_SAT32(int a, int b) {
        if (((a + b) & Integer.MIN_VALUE) != 0) {
            return (Integer.MIN_VALUE & (a | b)) == 0 ? Integer.MAX_VALUE : a + b;
        } else {
            if (((a & b) & Integer.MIN_VALUE) != 0) {
                return Integer.MIN_VALUE;
            }
            return a + b;
        }
    }

    static int SKP_SUB_SAT32(int a, int b) {
        if (((a - b) & Integer.MIN_VALUE) != 0) {
            return (Integer.MIN_VALUE & ((a ^ Integer.MIN_VALUE) & b)) != 0 ? Integer.MAX_VALUE : a - b;
        } else {
            if ((((b ^ Integer.MIN_VALUE) & a) & Integer.MIN_VALUE) != 0) {
                return Integer.MIN_VALUE;
            }
            return a - b;
        }
    }

    static int SKP_Silk_CLZ16(short in16) {
        return Integer.numberOfLeadingZeros(65535 & in16) - 16;
    }

    static int SKP_Silk_CLZ32(int in32) {
        return Integer.numberOfLeadingZeros(in32);
    }
}
