package org.jitsi.impl.neomedia.codec.audio.silk;

public class ResidualEnergyFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!ResidualEnergyFLP.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    static final int MAX_ITERATIONS_RESIDUAL_NRG = 10;
    static final float REGULARIZATION_FACTOR = 1.0E-8f;

    static float SKP_Silk_residual_energy_covar_FLP(float[] c, int c_offset, float[] wXX, int wXX_offset, float[] wXx, float wxx, int D) {
        float nrg = 0.0f;
        if ($assertionsDisabled || D >= 0) {
            float regularization = REGULARIZATION_FACTOR * (wXX[wXX_offset + 0] + wXX[((D * D) + wXX_offset) - 1]);
            int k = 0;
            while (k < 10) {
                int i;
                nrg = wxx;
                float tmp = 0.0f;
                for (i = 0; i < D; i++) {
                    tmp += wXx[i] * c[c_offset + i];
                }
                nrg -= 2.0f * tmp;
                for (i = 0; i < D; i++) {
                    tmp = 0.0f;
                    for (int j = i + 1; j < D; j++) {
                        tmp += wXX[(wXX_offset + i) + (j * D)] * c[c_offset + j];
                    }
                    nrg += c[c_offset + i] * ((2.0f * tmp) + (wXX[(wXX_offset + i) + (D * i)] * c[c_offset + i]));
                }
                if (nrg > 0.0f) {
                    break;
                }
                for (i = 0; i < D; i++) {
                    int i2 = (wXX_offset + i) + (D * i);
                    wXX[i2] = wXX[i2] + regularization;
                }
                regularization *= 2.0f;
                k++;
            }
            if (k != 10) {
                return nrg;
            }
            if ($assertionsDisabled || nrg == 0.0f) {
                return 1.0f;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }

    static void SKP_Silk_residual_energy_FLP(float[] nrgs, float[] x, float[][] a, float[] gains, int subfr_length, int LPC_order) {
        float[] LPC_res = new float[272];
        float[] LPC_res_ptr = LPC_res;
        int LPC_res_ptr_offset = LPC_order;
        int shift = LPC_order + subfr_length;
        LPCAnalysisFilterFLP.SKP_Silk_LPC_analysis_filter_FLP(LPC_res, a[0], x, (shift * 0) + 0, shift * 2, LPC_order);
        nrgs[0] = (float) (((double) (gains[0] * gains[0])) * EnergyFLP.SKP_Silk_energy_FLP(LPC_res_ptr, (shift * 0) + LPC_res_ptr_offset, subfr_length));
        nrgs[1] = (float) (((double) (gains[1] * gains[1])) * EnergyFLP.SKP_Silk_energy_FLP(LPC_res_ptr, (shift * 1) + LPC_res_ptr_offset, subfr_length));
        LPCAnalysisFilterFLP.SKP_Silk_LPC_analysis_filter_FLP(LPC_res, a[1], x, (shift * 2) + 0, shift * 2, LPC_order);
        nrgs[2] = (float) (((double) (gains[2] * gains[2])) * EnergyFLP.SKP_Silk_energy_FLP(LPC_res_ptr, (shift * 0) + LPC_res_ptr_offset, subfr_length));
        nrgs[3] = (float) (((double) (gains[3] * gains[3])) * EnergyFLP.SKP_Silk_energy_FLP(LPC_res_ptr, (shift * 1) + LPC_res_ptr_offset, subfr_length));
    }
}
