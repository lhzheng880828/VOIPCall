package org.jitsi.impl.neomedia.codec.audio.silk;

public class FindLPCFLP {
    static void SKP_Silk_find_LPC_FLP(float[] NLSF, int[] interpIndex, float[] prev_NLSFq, int useInterpNLSFs, int LPC_order, float[] x, int subfr_length) {
        float[] a = new float[16];
        float[] a_tmp = new float[16];
        float[] NLSF0 = new float[16];
        float[] LPC_res = new float[272];
        interpIndex[0] = 4;
        double res_nrg = (double) BurgModifiedFLP.SKP_Silk_burg_modified_FLP(a, x, 0, subfr_length, 4, 6.0E-5f, LPC_order);
        if (useInterpNLSFs == 1) {
            res_nrg -= (double) BurgModifiedFLP.SKP_Silk_burg_modified_FLP(a_tmp, x, subfr_length * 2, subfr_length, 2, 6.0E-5f, LPC_order);
            WrappersFLP.SKP_Silk_A2NLSF_FLP(NLSF, a_tmp, LPC_order);
            double res_nrg_2nd = 3.4028234663852886E38d;
            for (int k = 3; k >= 0; k--) {
                WrappersFLP.SKP_Silk_interpolate_wrapper_FLP(NLSF0, prev_NLSFq, NLSF, 0.25f * ((float) k), LPC_order);
                WrappersFLP.SKP_Silk_NLSF2A_stable_FLP(a_tmp, NLSF0, LPC_order);
                LPCAnalysisFilterFLP.SKP_Silk_LPC_analysis_filter_FLP(LPC_res, a_tmp, x, 0, subfr_length * 2, LPC_order);
                double res_nrg_interp = EnergyFLP.SKP_Silk_energy_FLP(LPC_res, LPC_order, subfr_length - LPC_order) + EnergyFLP.SKP_Silk_energy_FLP(LPC_res, LPC_order + subfr_length, subfr_length - LPC_order);
                if (res_nrg_interp < res_nrg) {
                    res_nrg = res_nrg_interp;
                    interpIndex[0] = k;
                } else if (res_nrg_interp > res_nrg_2nd) {
                    break;
                }
                res_nrg_2nd = res_nrg_interp;
            }
        }
        if (interpIndex[0] == 4) {
            WrappersFLP.SKP_Silk_A2NLSF_FLP(NLSF, a, LPC_order);
        }
    }
}
