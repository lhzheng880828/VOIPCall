package org.jitsi.impl.neomedia.codec.audio.silk;

import javax.media.CachingControl;

public class SigProcFIX extends SigProcFIXConstants {
    static int SKP_ROR32(int a32, int rot) {
        if (rot <= 0) {
            return (a32 << (-rot)) | (a32 >>> (rot + 32));
        }
        return (a32 << (32 - rot)) | (a32 >>> rot);
    }

    static int SKP_MUL(int a32, int b32) {
        return a32 * b32;
    }

    static long SKP_MUL_uint(long a32, long b32) {
        return a32 * b32;
    }

    static int SKP_MLA(int a32, int b32, int c32) {
        return (b32 * c32) + a32;
    }

    static long SKP_MLA_uint(long a32, long b32, long c32) {
        return (b32 * c32) + a32;
    }

    static int SKP_SMULTT(int a32, int b32) {
        return (a32 >> 16) * (b32 >> 16);
    }

    static int SKP_SMLATT(int a32, int b32, int c32) {
        return ((b32 >> 16) * (c32 >> 16)) + a32;
    }

    static long SKP_SMLALBB(long a64, short b16, short c16) {
        return ((long) (b16 * c16)) + a64;
    }

    static long SKP_SMULL(int a32, int b32) {
        return ((long) a32) * ((long) b32);
    }

    static int SKP_MLA_ovflw(int a32, int b32, int c32) {
        return (b32 * c32) + a32;
    }

    static int SKP_SMLABB_ovflw(int a32, int b32, int c32) {
        return (((short) b32) * ((short) c32)) + a32;
    }

    static int SKP_SMLABT_ovflw(int a32, int b32, int c32) {
        return (((short) b32) * (c32 >> 16)) + a32;
    }

    static int SKP_SMLATT_ovflw(int a32, int b32, int c32) {
        return ((b32 >> 16) * (c32 >> 16)) + a32;
    }

    static int SKP_SMLAWB_ovflw(int a32, int b32, int c32) {
        return (((b32 >> 16) * ((short) c32)) + (((65535 & b32) * ((short) c32)) >> 16)) + a32;
    }

    static int SKP_SMLAWT_ovflw(int a32, int b32, int c32) {
        return (((b32 >> 16) * (c32 >> 16)) + a32) + (((65535 & b32) * (c32 >> 16)) >> 16);
    }

    static long SKP_DIV64_32(long a64, int b32) {
        return a64 / ((long) b32);
    }

    static int SKP_DIV32_16(int a32, short b16) {
        return a32 / b16;
    }

    static int SKP_DIV32(int a32, int b32) {
        return a32 / b32;
    }

    static short SKP_ADD16(short a, short b) {
        return (short) (a + b);
    }

    static int SKP_ADD32(int a, int b) {
        return a + b;
    }

    static long SKP_ADD64(long a, long b) {
        return a + b;
    }

    static short SKP_SUB16(short a, short b) {
        return (short) (a - b);
    }

    static int SKP_SUB32(int a, int b) {
        return a - b;
    }

    static long SKP_SUB64(long a, long b) {
        return a - b;
    }

    static int SKP_SAT8(int a) {
        if (a > 127) {
            return 127;
        }
        return a < -128 ? -128 : a;
    }

    static int SKP_SAT16(int a) {
        if (a > 32767) {
            return 32767;
        }
        return a < -32768 ? -32768 : a;
    }

    static long SKP_SAT32(long a) {
        if (a > 2147483647L) {
            return 2147483647L;
        }
        return a < -2147483648L ? -2147483648L : a;
    }

    static byte SKP_CHECK_FIT8(int a) {
        return (byte) a;
    }

    static short SKP_CHECK_FIT16(int a) {
        return (short) a;
    }

    static int SKP_CHECK_FIT32(int a) {
        return a;
    }

    static short SKP_ADD_SAT16(short a, short b) {
        return (short) SKP_SAT16(a + b);
    }

    static long SKP_ADD_SAT64(long a, long b) {
        if (((a + b) & Long.MIN_VALUE) != 0) {
            return (Long.MIN_VALUE & (a | b)) == 0 ? CachingControl.LENGTH_UNKNOWN : a + b;
        } else {
            if (((a & b) & Long.MIN_VALUE) != 0) {
                return Long.MIN_VALUE;
            }
            return a + b;
        }
    }

    static short SKP_SUB_SAT16(short a, short b) {
        return (short) SKP_SAT16(a - b);
    }

    static long SKP_SUB_SAT64(long a, long b) {
        if (((a - b) & Long.MIN_VALUE) != 0) {
            return (Long.MIN_VALUE & ((a ^ Long.MIN_VALUE) & b)) != 0 ? CachingControl.LENGTH_UNKNOWN : a - b;
        } else {
            if ((((b ^ Long.MIN_VALUE) & a) & Long.MIN_VALUE) != 0) {
                return Long.MIN_VALUE;
            }
            return a - b;
        }
    }

    static long SKP_POS_SAT32(long a) {
        return a > 2147483647L ? 2147483647L : a;
    }

    static byte SKP_ADD_POS_SAT8(byte a, byte b) {
        return ((a + b) & 128) != 0 ? Byte.MAX_VALUE : (byte) (a + b);
    }

    static short SKP_ADD_POS_SAT16(short a, short b) {
        return ((a + b) & 32768) != 0 ? Short.MAX_VALUE : (short) (a + b);
    }

    static int SKP_ADD_POS_SAT32(int a, int b) {
        return ((a + b) & Integer.MIN_VALUE) != 0 ? Integer.MAX_VALUE : a + b;
    }

    static long SKP_ADD_POS_SAT64(long a, long b) {
        return ((a + b) & Long.MIN_VALUE) != 0 ? CachingControl.LENGTH_UNKNOWN : a + b;
    }

    static byte SKP_LSHIFT8(byte a, int shift) {
        return (byte) (a << shift);
    }

    static short SKP_LSHIFT16(short a, int shift) {
        return (short) (a << shift);
    }

    static int SKP_LSHIFT32(int a, int shift) {
        return a << shift;
    }

    static long SKP_LSHIFT64(long a, int shift) {
        return a << shift;
    }

    static int SKP_LSHIFT(int a, int shift) {
        return a << shift;
    }

    static byte SKP_RSHIFT8(byte a, int shift) {
        return (byte) (a >> shift);
    }

    static short SKP_RSHIFT16(short a, int shift) {
        return (short) (a >> shift);
    }

    static int SKP_RSHIFT32(int a, int shift) {
        return a >> shift;
    }

    static long SKP_RSHIFT64(long a, int shift) {
        return a >> shift;
    }

    static int SKP_RSHIFT(int a, int shift) {
        return a >> shift;
    }

    static short SKP_LSHIFT_SAT16(short a, int shift) {
        return SKP_LSHIFT16(SKP_LIMIT_16(a, (short) (-32768 >> shift), (short) (32767 >> shift)), shift);
    }

    static int SKP_LSHIFT_SAT32(int a, int shift) {
        return SKP_LSHIFT32(SKP_LIMIT(a, Integer.MIN_VALUE >> shift, Integer.MAX_VALUE >> shift), shift);
    }

    static int SKP_LSHIFT_ovflw(int a, int shift) {
        return a << shift;
    }

    static int SKP_LSHIFT_uint(int a, int shift) {
        return a << shift;
    }

    static int SKP_RSHIFT_uint(int a, int shift) {
        return a >>> shift;
    }

    static int SKP_ADD_LSHIFT(int a, int b, int shift) {
        return (b << shift) + a;
    }

    static int SKP_ADD_LSHIFT32(int a, int b, int shift) {
        return (b << shift) + a;
    }

    static int SKP_ADD_LSHIFT_uint(int a, int b, int shift) {
        return (b << shift) + a;
    }

    static int SKP_ADD_RSHIFT(int a, int b, int shift) {
        return (b >> shift) + a;
    }

    static int SKP_ADD_RSHIFT32(int a, int b, int shift) {
        return (b >> shift) + a;
    }

    static int SKP_ADD_RSHIFT_uint(int a, int b, int shift) {
        return (b >>> shift) + a;
    }

    static int SKP_SUB_LSHIFT32(int a, int b, int shift) {
        return a - (b << shift);
    }

    static int SKP_SUB_RSHIFT32(int a, int b, int shift) {
        return a - (b >> shift);
    }

    static int SKP_RSHIFT_ROUND(int a, int shift) {
        return shift == 1 ? (a >> 1) + (a & 1) : ((a >> (shift - 1)) + 1) >> 1;
    }

    static long SKP_RSHIFT_ROUND64(long a, int shift) {
        return shift == 1 ? (a >> 1) + (a & 1) : ((a >> (shift - 1)) + 1) >> 1;
    }

    static int SKP_NSHIFT_MUL_32_32(int a, int b) {
        return -(31 - ((32 - Macros.SKP_Silk_CLZ32(Math.abs(a))) + (32 - Macros.SKP_Silk_CLZ32(Math.abs(b)))));
    }

    static int SKP_NSHIFT_MUL_16_16(short a, short b) {
        return -(15 - ((16 - Macros.SKP_Silk_CLZ16((short) Math.abs(a))) + (16 - Macros.SKP_Silk_CLZ16((short) Math.abs(b)))));
    }

    static int SKP_min(int a, int b) {
        return a < b ? a : b;
    }

    static int SKP_max(int a, int b) {
        return a > b ? a : b;
    }

    static int SKP_FIX_CONST(float C, int Q) {
        return (int) (((double) (((float) (1 << Q)) * C)) + 0.5d);
    }

    static int SKP_min_int(int a, int b) {
        return a < b ? a : b;
    }

    static short SKP_min_16(short a, short b) {
        return a < b ? a : b;
    }

    static int SKP_min_32(int a, int b) {
        return a < b ? a : b;
    }

    static long SKP_min_64(long a, long b) {
        return a < b ? a : b;
    }

    static int SKP_max_int(int a, int b) {
        return a > b ? a : b;
    }

    static short SKP_max_16(short a, short b) {
        return a > b ? a : b;
    }

    static int SKP_max_32(int a, int b) {
        return a > b ? a : b;
    }

    static long SKP_max_64(long a, long b) {
        return a > b ? a : b;
    }

    static int SKP_LIMIT(int a, int limit1, int limit2) {
        if (limit1 <= limit2) {
            if (a <= limit2) {
                limit2 = a < limit1 ? limit1 : a;
            }
            return limit2;
        } else if (a > limit1) {
            return limit1;
        } else {
            return a < limit2 ? limit2 : a;
        }
    }

    static float SKP_LIMIT(float a, float limit1, float limit2) {
        if (limit1 <= limit2) {
            if (a <= limit2) {
                limit2 = a < limit1 ? limit1 : a;
            }
            return limit2;
        } else if (a > limit1) {
            return limit1;
        } else {
            return a < limit2 ? limit2 : a;
        }
    }

    static int SKP_LIMIT_int(int a, int limit1, int limit2) {
        if (limit1 <= limit2) {
            if (a <= limit2) {
                limit2 = a < limit1 ? limit1 : a;
            }
            return limit2;
        } else if (a > limit1) {
            return limit1;
        } else {
            return a < limit2 ? limit2 : a;
        }
    }

    static short SKP_LIMIT_16(short a, short limit1, short limit2) {
        if (limit1 <= limit2) {
            if (a <= limit2) {
                limit2 = a < limit1 ? limit1 : a;
            }
            return limit2;
        } else if (a > limit1) {
            return limit1;
        } else {
            return a < limit2 ? limit2 : a;
        }
    }

    static int SKP_LIMIT_32(int a, int limit1, int limit2) {
        if (limit1 <= limit2) {
            if (a <= limit2) {
                limit2 = a < limit1 ? limit1 : a;
            }
            return limit2;
        } else if (a > limit1) {
            return limit1;
        } else {
            return a < limit2 ? limit2 : a;
        }
    }

    static int SKP_abs(int a) {
        return a > 0 ? a : -a;
    }

    static int SKP_abs_int(int a) {
        return ((a >> 31) ^ a) - (a >> 31);
    }

    static int SKP_abs_int32(int a) {
        return ((a >> 31) ^ a) - (a >> 31);
    }

    static long SKP_abs_int64(long a) {
        return a > 0 ? a : -a;
    }

    static int SKP_sign(int a) {
        if (a > 0) {
            return 1;
        }
        return a < 0 ? -1 : 0;
    }

    static double SKP_sqrt(int a) {
        return Math.sqrt((double) a);
    }

    static int SKP_RAND(int seed) {
        return 907633515 + (196314165 * seed);
    }

    static int SKP_SMMUL(int a32, int b32) {
        return (int) ((((long) a32) * ((long) b32)) >> 32);
    }
}
