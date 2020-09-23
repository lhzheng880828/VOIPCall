package org.jitsi.impl.neomedia.codec.audio.silk;

import javax.media.Buffer;

public class A2NLSF extends A2NLSF_constants {
    static final /* synthetic */ boolean $assertionsDisabled = (!A2NLSF.class.desiredAssertionStatus());

    static void SKP_Silk_A2NLSF_trans_poly(int[] p, int dd) {
        for (int k = 2; k <= dd; k++) {
            int i;
            for (int n = dd; n > k; n--) {
                i = n - 2;
                p[i] = p[i] - p[n];
            }
            i = k - 2;
            p[i] = p[i] - (p[k] << 1);
        }
    }

    static int SKP_Silk_A2NLSF_eval_poly(int[] p, int x, int dd) {
        int y32 = p[dd];
        int x_Q16 = x << 4;
        for (int n = dd - 1; n >= 0; n--) {
            y32 = Macros.SKP_SMLAWW(p[n], y32, x_Q16);
        }
        return y32;
    }

    static void SKP_Silk_A2NLSF_init(int[] a_Q16, int[] P, int[] Q, int dd) {
        int k;
        P[dd] = Buffer.FLAG_SKIP_FEC;
        Q[dd] = Buffer.FLAG_SKIP_FEC;
        for (k = 0; k < dd; k++) {
            P[k] = (-a_Q16[(dd - k) - 1]) - a_Q16[dd + k];
            Q[k] = (-a_Q16[(dd - k) - 1]) + a_Q16[dd + k];
        }
        for (k = dd; k > 0; k--) {
            int i = k - 1;
            P[i] = P[i] - P[k];
            i = k - 1;
            Q[i] = Q[i] + Q[k];
        }
        SKP_Silk_A2NLSF_trans_poly(P, dd);
        SKP_Silk_A2NLSF_trans_poly(Q, dd);
    }

    static void SKP_Silk_A2NLSF(int[] NLSF, int[] a_Q16, int d) {
        int root_ix;
        int[] P = new int[9];
        int[] Q = new int[9];
        int[][] PQ = new int[][]{P, Q};
        int dd = d >> 1;
        SKP_Silk_A2NLSF_init(a_Q16, P, Q, dd);
        int[] p = P;
        int xlo = LSFCosTable.SKP_Silk_LSFCosTab_FIX_Q12[0];
        int ylo = SKP_Silk_A2NLSF_eval_poly(p, xlo, dd);
        if (ylo < 0) {
            NLSF[0] = 0;
            p = Q;
            ylo = SKP_Silk_A2NLSF_eval_poly(p, xlo, dd);
            root_ix = 1;
        } else {
            root_ix = 0;
        }
        int k = 1;
        int i = 0;
        while (true) {
            int xhi = LSFCosTable.SKP_Silk_LSFCosTab_FIX_Q12[k];
            int yhi = SKP_Silk_A2NLSF_eval_poly(p, xhi, dd);
            if ((ylo > 0 || yhi < 0) && (ylo < 0 || yhi > 0)) {
                k++;
                xlo = xhi;
                ylo = yhi;
                if (k > 128) {
                    i++;
                    if (i > 50) {
                        NLSF[0] = 32768 / (d + 1);
                        for (k = 1; k < d; k++) {
                            NLSF[k] = Macros.SKP_SMULBB(k + 1, NLSF[0]);
                        }
                        return;
                    }
                    Bwexpander32.SKP_Silk_bwexpander_32(a_Q16, d, Buffer.FLAG_SKIP_FEC - Macros.SKP_SMULBB(66, i));
                    SKP_Silk_A2NLSF_init(a_Q16, P, Q, dd);
                    p = P;
                    xlo = LSFCosTable.SKP_Silk_LSFCosTab_FIX_Q12[0];
                    ylo = SKP_Silk_A2NLSF_eval_poly(p, xlo, dd);
                    if (ylo < 0) {
                        NLSF[0] = 0;
                        p = Q;
                        ylo = SKP_Silk_A2NLSF_eval_poly(p, xlo, dd);
                        root_ix = 1;
                    } else {
                        root_ix = 0;
                    }
                    k = 1;
                } else {
                    continue;
                }
            } else {
                int ffrac = -256;
                for (int m = 0; m < 2; m++) {
                    int xmid = SigProcFIX.SKP_RSHIFT_ROUND(xlo + xhi, 1);
                    int ymid = SKP_Silk_A2NLSF_eval_poly(p, xmid, dd);
                    if ((ylo > 0 || ymid < 0) && (ylo < 0 || ymid > 0)) {
                        xlo = xmid;
                        ylo = ymid;
                        ffrac += 128 >> m;
                    } else {
                        xhi = xmid;
                        yhi = ymid;
                    }
                }
                if (Math.abs(ylo) < 65536) {
                    int den = ylo - yhi;
                    int nom = (ylo << 6) + (den >> 1);
                    if (den != 0) {
                        ffrac += nom / den;
                    }
                } else {
                    ffrac += ylo / ((ylo - yhi) >> 6);
                }
                NLSF[root_ix] = Math.min((k << 8) + ffrac, 32767);
                if (!$assertionsDisabled && NLSF[root_ix] < 0) {
                    throw new AssertionError();
                } else if ($assertionsDisabled || NLSF[root_ix] <= 32767) {
                    root_ix++;
                    if (root_ix < d) {
                        p = PQ[root_ix & 1];
                        xlo = LSFCosTable.SKP_Silk_LSFCosTab_FIX_Q12[k - 1];
                        ylo = (1 - (root_ix & 2)) << 12;
                    } else {
                        return;
                    }
                } else {
                    throw new AssertionError();
                }
            }
        }
    }
}
