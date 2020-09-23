package org.jitsi.impl.neomedia.codec.audio.silk;

public class Inlines extends InlinesConstants {
    static final /* synthetic */ boolean $assertionsDisabled = (!Inlines.class.desiredAssertionStatus());

    static int SKP_Silk_CLZ64(long in) {
        return Long.numberOfLeadingZeros(in);
    }

    static void SKP_Silk_CLZ_FRAC(int in, int[] lz, int[] frac_Q7) {
        int lzeros = Integer.numberOfLeadingZeros(in);
        lz[0] = lzeros;
        frac_Q7[0] = SigProcFIX.SKP_ROR32(in, 24 - lzeros) & 127;
    }

    static int SKP_Silk_SQRT_APPROX(int x) {
        int[] lz = new int[1];
        int[] frac_Q7 = new int[1];
        if (x <= 0) {
            return 0;
        }
        int y;
        SKP_Silk_CLZ_FRAC(x, lz, frac_Q7);
        if ((lz[0] & 1) != 0) {
            y = 32768;
        } else {
            y = 46214;
        }
        y >>= lz[0] >> 1;
        return Macros.SKP_SMLAWB(y, y, Macros.SKP_SMULBB(213, frac_Q7[0]));
    }

    static int SKP_Silk_norm16(short a) {
        if ((a << 1) == 0) {
            return 0;
        }
        short a32 = a;
        return Integer.numberOfLeadingZeros(a32 ^ (a32 >> 31)) - 17;
    }

    static int SKP_Silk_norm32(int a) {
        if ((a << 1) == 0) {
            return 0;
        }
        return Integer.numberOfLeadingZeros(a ^ (a >> 31)) - 1;
    }

    static int SKP_DIV32_varQ(int a32, int b32, int Qres) {
        if (!$assertionsDisabled && b32 == 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || Qres >= 0) {
            int a_headrm = Integer.numberOfLeadingZeros(Math.abs(a32)) - 1;
            int a32_nrm = a32 << a_headrm;
            int b_headrm = Integer.numberOfLeadingZeros(Math.abs(b32)) - 1;
            int b32_nrm = b32 << b_headrm;
            int b32_inv = 536870911 / (b32_nrm >> 16);
            int result = Macros.SKP_SMULWB(a32_nrm, b32_inv);
            result = Macros.SKP_SMLAWB(result, a32_nrm - (SigProcFIX.SKP_SMMUL(b32_nrm, result) << 3), b32_inv);
            int lshift = ((a_headrm + 29) - b_headrm) - Qres;
            if (lshift <= 0) {
                return SigProcFIX.SKP_LSHIFT_SAT32(result, -lshift);
            }
            if (lshift < 32) {
                return result >> lshift;
            }
            return 0;
        } else {
            throw new AssertionError();
        }
    }

    static int SKP_INVERSE32_varQ(int b32, int Qres) {
        if (!$assertionsDisabled && b32 == 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || Qres > 0) {
            int b_headrm = Integer.numberOfLeadingZeros(Math.abs(b32)) - 1;
            int b32_nrm = b32 << b_headrm;
            int b32_inv = 536870911 / (b32_nrm >> 16);
            int result = Macros.SKP_SMLAWW(b32_inv << 16, (-Macros.SKP_SMULWB(b32_nrm, b32_inv)) << 3, b32_inv);
            int lshift = (61 - b_headrm) - Qres;
            if (lshift <= 0) {
                return SigProcFIX.SKP_LSHIFT_SAT32(result, -lshift);
            }
            if (lshift < 32) {
                return result >> lshift;
            }
            return 0;
        } else {
            throw new AssertionError();
        }
    }

    static int SKP_Silk_SIN_APPROX_Q24(int x) {
        int y_Q30;
        x &= 65535;
        if (x <= 32768) {
            if (x < 16384) {
                x = 16384 - x;
            } else {
                x -= 16384;
            }
            if (x < 1100) {
                return Macros.SKP_SMLAWB(16777216, x * x, -5053);
            }
            x = Macros.SKP_SMULWB(x << 8, x);
            y_Q30 = Macros.SKP_SMLAWW(1073735466, x, Macros.SKP_SMLAWW(-82778932, x, Macros.SKP_SMLAWB(1059577, x, -5013)));
        } else {
            if (x < 49152) {
                x = 49152 - x;
            } else {
                x -= 49152;
            }
            if (x < 1100) {
                return Macros.SKP_SMLAWB(-16777216, x * x, 5053);
            }
            x = Macros.SKP_SMULWB(x << 8, x);
            y_Q30 = Macros.SKP_SMLAWW(-1073735400, x, Macros.SKP_SMLAWW(82778932, x, Macros.SKP_SMLAWB(-1059577, x, 5013)));
        }
        return SigProcFIX.SKP_RSHIFT_ROUND(y_Q30, 6);
    }

    static int SKP_Silk_COS_APPROX_Q24(int x) {
        return SKP_Silk_SIN_APPROX_Q24(x + 16384);
    }
}
