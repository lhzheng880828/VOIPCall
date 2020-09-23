package org.jitsi.impl.neomedia.codec.audio.silk;

public class NLSF2A {
    static void SKP_Silk_NLSF2A_find_poly(int[] out, int[] cLSF, int cLSF_offset, int dd) {
        out[0] = 1048576;
        out[1] = -cLSF[cLSF_offset + 0];
        for (int k = 1; k < dd; k++) {
            int ftmp = cLSF[(k * 2) + cLSF_offset];
            int test = ftmp * out[k];
            long test2 = SigProcFIX.SKP_SMULL(ftmp, out[k]);
            out[k + 1] = (out[k - 1] << 1) - ((int) SigProcFIX.SKP_RSHIFT_ROUND64(SigProcFIX.SKP_SMULL(ftmp, out[k]), 20));
            for (int n = k; n > 1; n--) {
                out[n] = out[n] + (out[n - 2] - ((int) SigProcFIX.SKP_RSHIFT_ROUND64(SigProcFIX.SKP_SMULL(ftmp, out[n - 1]), 20)));
            }
            out[1] = out[1] - ftmp;
        }
    }

    static void SKP_Silk_NLSF2A(short[] a, int[] NLSF, int d) {
        int k;
        int[] cos_LSF_Q20 = new int[16];
        int[] P = new int[9];
        int[] Q = new int[9];
        int[] a_int32 = new int[16];
        int idx = 0;
        Typedef.SKP_assert(true);
        for (k = 0; k < d; k++) {
            Typedef.SKP_assert(NLSF[k] >= 0);
            Typedef.SKP_assert(NLSF[k] <= 32767);
            int f_int = NLSF[k] >> 8;
            int f_frac = NLSF[k] - (f_int << 8);
            Typedef.SKP_assert(f_int >= 0);
            Typedef.SKP_assert(f_int < 128);
            int cos_val = LSFCosTable.SKP_Silk_LSFCosTab_FIX_Q12[f_int];
            cos_LSF_Q20[k] = (cos_val << 8) + ((LSFCosTable.SKP_Silk_LSFCosTab_FIX_Q12[f_int + 1] - cos_val) * f_frac);
        }
        int dd = d >> 1;
        SKP_Silk_NLSF2A_find_poly(P, cos_LSF_Q20, 0, dd);
        SKP_Silk_NLSF2A_find_poly(Q, cos_LSF_Q20, 1, dd);
        for (k = 0; k < dd; k++) {
            int Ptmp = P[k + 1] + P[k];
            int Qtmp = Q[k + 1] - Q[k];
            a_int32[k] = -SigProcFIX.SKP_RSHIFT_ROUND(Ptmp + Qtmp, 9);
            a_int32[(d - k) - 1] = SigProcFIX.SKP_RSHIFT_ROUND(Qtmp - Ptmp, 9);
        }
        int i = 0;
        while (i < 10) {
            int maxabs = 0;
            for (k = 0; k < d; k++) {
                int absval = SigProcFIX.SKP_abs(a_int32[k]);
                if (absval > maxabs) {
                    maxabs = absval;
                    idx = k;
                }
            }
            if (maxabs <= 32767) {
                break;
            }
            maxabs = SigProcFIX.SKP_min(maxabs, 98369);
            Bwexpander32.SKP_Silk_bwexpander_32(a_int32, d, 65470 - (((maxabs - 32767) * 16367) / (((idx + 1) * maxabs) >> 2)));
            i++;
        }
        if (i == 10) {
            Typedef.SKP_assert(false);
            for (k = 0; k < d; k++) {
                a_int32[k] = SigProcFIX.SKP_SAT16(a_int32[k]);
            }
        }
        for (k = 0; k < d; k++) {
            a[k] = (short) a_int32[k];
        }
    }
}
