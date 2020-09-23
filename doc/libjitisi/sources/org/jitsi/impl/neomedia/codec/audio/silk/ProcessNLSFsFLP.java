package org.jitsi.impl.neomedia.codec.audio.silk;

public class ProcessNLSFsFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!ProcessNLSFsFLP.class.desiredAssertionStatus());

    static void SKP_Silk_process_NLSFs_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control_FLP psEncCtrl, float[] pNLSF) {
        float[] pNLSFW = new float[16];
        float NLSF_interpolation_factor = 0.0f;
        float[] pNLSF0_temp = new float[16];
        float[] pNLSFW0_temp = new float[16];
        if ($assertionsDisabled || psEncCtrl.sCmn.sigtype == 0 || psEncCtrl.sCmn.sigtype == 1) {
            float NLSF_mu;
            float NLSF_mu_fluc_red;
            if (psEncCtrl.sCmn.sigtype == 0) {
                NLSF_mu = 0.002f - (0.001f * psEnc.speech_activity);
                NLSF_mu_fluc_red = 0.1f - (0.05f * psEnc.speech_activity);
            } else {
                NLSF_mu = 0.005f - (0.004f * psEnc.speech_activity);
                NLSF_mu_fluc_red = 0.2f - (0.1f * (psEnc.speech_activity + psEncCtrl.sparseness));
            }
            NLSFVQWeightsLaroiaFLP.SKP_Silk_NLSF_VQ_weights_laroia_FLP(pNLSFW, pNLSF, psEnc.sCmn.predictLPCOrder);
            boolean doInterpolate = psEnc.sCmn.useInterpolatedNLSFs == 1 && psEncCtrl.sCmn.NLSFInterpCoef_Q2 < 4;
            if (doInterpolate) {
                NLSF_interpolation_factor = 0.25f * ((float) psEncCtrl.sCmn.NLSFInterpCoef_Q2);
                WrappersFLP.SKP_Silk_interpolate_wrapper_FLP(pNLSF0_temp, psEnc.sPred.prev_NLSFq, pNLSF, NLSF_interpolation_factor, psEnc.sCmn.predictLPCOrder);
                NLSFVQWeightsLaroiaFLP.SKP_Silk_NLSF_VQ_weights_laroia_FLP(pNLSFW0_temp, pNLSF0_temp, psEnc.sCmn.predictLPCOrder);
                float i_sqr = NLSF_interpolation_factor * NLSF_interpolation_factor;
                for (int i = 0; i < psEnc.sCmn.predictLPCOrder; i++) {
                    pNLSFW[i] = 0.5f * (pNLSFW[i] + (pNLSFW0_temp[i] * i_sqr));
                }
            }
            NLSFMSVQEncodeFLP.SKP_Silk_NLSF_MSVQ_encode_FLP(psEncCtrl.sCmn.NLSFIndices, pNLSF, psEnc.psNLSF_CB_FLP[psEncCtrl.sCmn.sigtype], psEnc.sPred.prev_NLSFq, pNLSFW, NLSF_mu, NLSF_mu_fluc_red, psEnc.sCmn.NLSF_MSVQ_Survivors, psEnc.sCmn.predictLPCOrder, psEnc.sCmn.first_frame_after_reset);
            WrappersFLP.SKP_Silk_NLSF2A_stable_FLP(psEncCtrl.PredCoef[1], pNLSF, psEnc.sCmn.predictLPCOrder);
            if (doInterpolate) {
                WrappersFLP.SKP_Silk_interpolate_wrapper_FLP(pNLSF0_temp, psEnc.sPred.prev_NLSFq, pNLSF, NLSF_interpolation_factor, psEnc.sCmn.predictLPCOrder);
                WrappersFLP.SKP_Silk_NLSF2A_stable_FLP(psEncCtrl.PredCoef[0], pNLSF0_temp, psEnc.sCmn.predictLPCOrder);
                return;
            }
            System.arraycopy(psEncCtrl.PredCoef[1], 0, psEncCtrl.PredCoef[0], 0, psEnc.sCmn.predictLPCOrder);
            return;
        }
        throw new AssertionError();
    }
}
