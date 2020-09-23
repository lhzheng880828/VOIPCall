package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.portaudio.Pa;

public class BurgModifiedFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!BurgModifiedFLP.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    static final int MAX_FRAME_SIZE = 544;
    static final int MAX_NB_SUBFR = 4;

    static float SKP_Silk_burg_modified_FLP(float[] A, float[] x, int x_offset, int subfr_length, int nb_subfr, float WhiteNoiseFrac, int D) {
        double[] C_first_row = new double[16];
        double[] C_last_row = new double[16];
        double[] CAf = new double[17];
        double[] CAb = new double[17];
        double[] Af = new double[16];
        if (!$assertionsDisabled && subfr_length * nb_subfr > MAX_FRAME_SIZE) {
            throw new AssertionError();
        } else if ($assertionsDisabled || nb_subfr <= 4) {
            int s;
            float[] x_ptr;
            int x_ptr_offset;
            int n;
            int i;
            double tmp1;
            int k;
            double Atmp;
            double nrg_f;
            double C0 = EnergyFLP.SKP_Silk_energy_FLP(x, x_offset, nb_subfr * subfr_length);
            for (s = 0; s < nb_subfr; s++) {
                x_ptr = x;
                x_ptr_offset = x_offset + (s * subfr_length);
                for (n = 1; n < D + 1; n++) {
                    i = n - 1;
                    C_first_row[i] = C_first_row[i] + InnerProductFLP.SKP_Silk_inner_product_FLP(x_ptr, x_ptr_offset, x_ptr, x_ptr_offset + n, subfr_length - n);
                }
            }
            System.arraycopy(C_first_row, 0, C_last_row, 0, 16);
            double d = ((((double) WhiteNoiseFrac) * C0) + C0) + 9.999999717180685E-10d;
            CAf[0] = d;
            CAb[0] = d;
            n = 0;
            while (n < D) {
                double tmp2;
                for (s = 0; s < nb_subfr; s++) {
                    x_ptr = x;
                    x_ptr_offset = x_offset + (s * subfr_length);
                    tmp1 = (double) x_ptr[x_ptr_offset + n];
                    tmp2 = (double) x_ptr[((x_ptr_offset + subfr_length) - n) - 1];
                    for (k = 0; k < n; k++) {
                        C_first_row[k] = C_first_row[k] - ((double) (x_ptr[x_ptr_offset + n] * x_ptr[((x_ptr_offset + n) - k) - 1]));
                        C_last_row[k] = C_last_row[k] - ((double) (x_ptr[((x_ptr_offset + subfr_length) - n) - 1] * x_ptr[((x_ptr_offset + subfr_length) - n) + k]));
                        Atmp = Af[k];
                        tmp1 += ((double) x_ptr[((x_ptr_offset + n) - k) - 1]) * Atmp;
                        tmp2 += ((double) x_ptr[((x_ptr_offset + subfr_length) - n) + k]) * Atmp;
                    }
                    for (k = 0; k <= n; k++) {
                        CAf[k] = CAf[k] - (((double) x_ptr[(x_ptr_offset + n) - k]) * tmp1);
                        CAb[k] = CAb[k] - (((double) x_ptr[(((x_ptr_offset + subfr_length) - n) + k) - 1]) * tmp2);
                    }
                }
                tmp1 = C_first_row[n];
                tmp2 = C_last_row[n];
                for (k = 0; k < n; k++) {
                    Atmp = Af[k];
                    tmp1 += C_last_row[(n - k) - 1] * Atmp;
                    tmp2 += C_first_row[(n - k) - 1] * Atmp;
                }
                CAf[n + 1] = tmp1;
                CAb[n + 1] = tmp2;
                double num = CAb[n + 1];
                double nrg_b = CAb[0];
                nrg_f = CAf[0];
                for (k = 0; k < n; k++) {
                    Atmp = Af[k];
                    num += CAb[n - k] * Atmp;
                    nrg_b += CAb[k + 1] * Atmp;
                    nrg_f += CAf[k + 1] * Atmp;
                }
                if (!$assertionsDisabled && nrg_f <= Pa.LATENCY_UNSPECIFIED) {
                    throw new AssertionError();
                } else if ($assertionsDisabled || nrg_b > Pa.LATENCY_UNSPECIFIED) {
                    double rc = (-2.0d * num) / (nrg_f + nrg_b);
                    if ($assertionsDisabled || (rc > -1.0d && rc < 1.0d)) {
                        for (k = 0; k < ((n + 1) >> 1); k++) {
                            tmp1 = Af[k];
                            tmp2 = Af[(n - k) - 1];
                            Af[k] = (rc * tmp2) + tmp1;
                            Af[(n - k) - 1] = (rc * tmp1) + tmp2;
                        }
                        Af[n] = rc;
                        for (k = 0; k <= n + 1; k++) {
                            tmp1 = CAf[k];
                            CAf[k] = CAf[k] + (CAb[(n - k) + 1] * rc);
                            i = (n - k) + 1;
                            CAb[i] = CAb[i] + (rc * tmp1);
                        }
                        n++;
                    } else {
                        throw new AssertionError();
                    }
                } else {
                    throw new AssertionError();
                }
            }
            nrg_f = CAf[0];
            tmp1 = 1.0d;
            for (k = 0; k < D; k++) {
                Atmp = Af[k];
                nrg_f += CAf[k + 1] * Atmp;
                tmp1 += Atmp * Atmp;
                A[k] = (float) (-Atmp);
            }
            return (float) (nrg_f - ((((double) WhiteNoiseFrac) * C0) * tmp1));
        } else {
            throw new AssertionError();
        }
    }
}
