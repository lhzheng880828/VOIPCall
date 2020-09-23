package org.jitsi.impl.neomedia.codec.audio.silk;

public class FindLTPFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!FindLTPFLP.class.desiredAssertionStatus());

    static void SKP_Silk_find_LTP_FLP(float[] b, float[] WLTP, float[] LTPredCodGain, float[] r_first, float[] r_last, int r_last_offset, int[] lag, float[] Wght, int subfr_length, int mem_offset) {
        int k;
        int i;
        float[] d = new float[4];
        float[] delta_b = new float[5];
        float[] w = new float[4];
        float[] nrg = new float[4];
        float[] Rr = new float[5];
        float[] rr = new float[4];
        float[] b_ptr = b;
        int b_ptr_offset = 0;
        float[] WLTP_ptr = WLTP;
        int WLTP_ptr_offset = 0;
        float[] r_ptr = r_first;
        int r_ptr_offset = mem_offset;
        for (k = 0; k < 4; k++) {
            if (k == 2) {
                r_ptr = r_last;
                r_ptr_offset = r_last_offset + mem_offset;
            }
            float[] lag_ptr = r_ptr;
            int lag_ptr_offset = r_ptr_offset - (lag[k] + 2);
            CorrMatrixFLP.SKP_Silk_corrMatrix_FLP(lag_ptr, lag_ptr_offset, subfr_length, 5, WLTP_ptr, WLTP_ptr_offset);
            CorrMatrixFLP.SKP_Silk_corrVector_FLP(lag_ptr, lag_ptr_offset, r_ptr, r_ptr_offset, subfr_length, 5, Rr);
            rr[k] = (float) EnergyFLP.SKP_Silk_energy_FLP(r_ptr, r_ptr_offset, subfr_length);
            RegularizeCorrelationsFLP.SKP_Silk_regularize_correlations_FLP(WLTP_ptr, WLTP_ptr_offset, rr, k, 0.001f * (rr[k] + 1.0f), 5);
            SolveLSFLP.SKP_Silk_solve_LDL_FLP(WLTP_ptr, WLTP_ptr_offset, 5, Rr, b_ptr, b_ptr_offset);
            nrg[k] = ResidualEnergyFLP.SKP_Silk_residual_energy_covar_FLP(b_ptr, b_ptr_offset, WLTP_ptr, WLTP_ptr_offset, Rr, rr[k], 5);
            ScaleVectorFLP.SKP_Silk_scale_vector_FLP(WLTP_ptr, WLTP_ptr_offset, Wght[k] / ((nrg[k] * Wght[k]) + (0.01f * ((float) subfr_length))), 25);
            w[k] = WLTP_ptr[WLTP_ptr_offset + 12];
            r_ptr_offset += subfr_length;
            b_ptr_offset += 5;
            WLTP_ptr_offset += 25;
        }
        if (LTPredCodGain != null) {
            float LPC_LTP_res_nrg = 1.0E-6f;
            float LPC_res_nrg = 0.0f;
            for (k = 0; k < 4; k++) {
                LPC_res_nrg += rr[k] * Wght[k];
                LPC_LTP_res_nrg += nrg[k] * Wght[k];
            }
            if ($assertionsDisabled || LPC_LTP_res_nrg > 0.0f) {
                LTPredCodGain[0] = 3.0f * MainFLP.SKP_Silk_log2((double) (LPC_res_nrg / LPC_LTP_res_nrg));
            } else {
                throw new AssertionError();
            }
        }
        b_ptr = b;
        b_ptr_offset = 0;
        for (k = 0; k < 4; k++) {
            d[k] = 0.0f;
            for (i = 0; i < 5; i++) {
                d[k] = d[k] + b_ptr[b_ptr_offset + i];
            }
            b_ptr_offset += 5;
        }
        float temp = 0.001f;
        for (k = 0; k < 4; k++) {
            temp += w[k];
        }
        float m = 0.0f;
        for (k = 0; k < 4; k++) {
            m += d[k] * w[k];
        }
        m /= temp;
        b_ptr = b;
        b_ptr_offset = 0;
        for (k = 0; k < 4; k++) {
            float g = (0.1f / (0.1f + w[k])) * (m - d[k]);
            temp = 0.0f;
            for (i = 0; i < 5; i++) {
                delta_b[i] = Math.max(b_ptr[i], 0.1f);
                temp += delta_b[i];
            }
            temp = g / temp;
            for (i = 0; i < 5; i++) {
                b_ptr[i] = b_ptr[i] + (delta_b[i] * temp);
            }
            b_ptr_offset += 5;
        }
    }
}
