package org.jitsi.impl.neomedia.codec.audio.silk;

public class NLSFVQRateDistortionFLP {
    static void SKP_Silk_NLSF_VQ_rate_distortion_FLP(float[] pRD, SKP_Silk_NLSF_CBS_FLP psNLSF_CBS_FLP, float[] in, float[] w, float[] rate_acc, float mu, int N, int LPC_order) {
        NLSFVQSumErrorFLP.SKP_Silk_NLSF_VQ_sum_error_FLP(pRD, in, w, psNLSF_CBS_FLP.CB, N, psNLSF_CBS_FLP.nVectors, LPC_order);
        float[] pRD_vec = pRD;
        int pRD_vec_offset = 0;
        for (int n = 0; n < N; n++) {
            for (int i = 0; i < psNLSF_CBS_FLP.nVectors; i++) {
                int i2 = pRD_vec_offset + i;
                pRD_vec[i2] = pRD_vec[i2] + ((rate_acc[n] + psNLSF_CBS_FLP.Rates[i]) * mu);
            }
            pRD_vec_offset += psNLSF_CBS_FLP.nVectors;
        }
    }
}
