package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;

public class FindPredCoefsFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!FindPredCoefsFLP.class.desiredAssertionStatus());
    static int frame_cnt = 0;

    static void SKP_Silk_find_pred_coefs_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control_FLP psEncCtrl, float[] res_pitch) {
        float[] WLTP = new float[100];
        float[] invGains = new float[4];
        float[] Wght = new float[4];
        Object NLSF = new float[16];
        float[] LPC_in_pre = new float[544];
        int i = 0;
        while (i < 4) {
            if ($assertionsDisabled || psEncCtrl.Gains[i] > 0.0f) {
                invGains[i] = 1.0f / psEncCtrl.Gains[i];
                Wght[i] = invGains[i] * invGains[i];
                i++;
            } else {
                throw new AssertionError();
            }
        }
        if (psEncCtrl.sCmn.sigtype != 0) {
            float[] x_ptr = psEnc.x_buf;
            int x_ptr_offset = psEnc.sCmn.frame_length - psEnc.sCmn.predictLPCOrder;
            float[] x_pre_ptr = LPC_in_pre;
            int x_pre_ptr_offset = 0;
            for (i = 0; i < 4; i++) {
                ScaleCopyVectorFLP.SKP_Silk_scale_copy_vector_FLP(x_pre_ptr, x_pre_ptr_offset, x_ptr, x_ptr_offset, invGains[i], psEnc.sCmn.subfr_length + psEnc.sCmn.predictLPCOrder);
                x_pre_ptr_offset += psEnc.sCmn.subfr_length + psEnc.sCmn.predictLPCOrder;
                x_ptr_offset += psEnc.sCmn.subfr_length;
            }
            Arrays.fill(psEncCtrl.LTPCoef, 0, 20, 0.0f);
            psEncCtrl.LTPredCodGain = 0.0f;
        } else if ($assertionsDisabled || psEnc.sCmn.frame_length - psEnc.sCmn.predictLPCOrder >= psEncCtrl.sCmn.pitchL[0] + 2) {
            float[] LTPredCodGain_ptr = new float[]{psEncCtrl.LTPredCodGain};
            FindLTPFLP.SKP_Silk_find_LTP_FLP(psEncCtrl.LTPCoef, WLTP, LTPredCodGain_ptr, res_pitch, res_pitch, psEnc.sCmn.frame_length >> 1, psEncCtrl.sCmn.pitchL, Wght, psEnc.sCmn.subfr_length, psEnc.sCmn.frame_length);
            psEncCtrl.LTPredCodGain = LTPredCodGain_ptr[0];
            int[] PERIndex_ptr = new int[]{psEncCtrl.sCmn.PERIndex};
            QuantLTPGainsFLP.SKP_Silk_quant_LTP_gains_FLP(psEncCtrl.LTPCoef, psEncCtrl.sCmn.LTPIndex, PERIndex_ptr, WLTP, psEnc.mu_LTP, psEnc.sCmn.LTPQuantLowComplexity);
            psEncCtrl.sCmn.PERIndex = PERIndex_ptr[0];
            LTPScaleCtrlFLP.SKP_Silk_LTP_scale_ctrl_FLP(psEnc, psEncCtrl);
            LTPAnalysisFilterFLP.SKP_Silk_LTP_analysis_filter_FLP(LPC_in_pre, psEnc.x_buf, psEnc.sCmn.frame_length - psEnc.sCmn.predictLPCOrder, psEncCtrl.LTPCoef, psEncCtrl.sCmn.pitchL, invGains, psEnc.sCmn.subfr_length, psEnc.sCmn.predictLPCOrder);
        } else {
            throw new AssertionError();
        }
        int[] NLSFInterpCoef_Q2_ptr = new int[]{psEncCtrl.sCmn.NLSFInterpCoef_Q2};
        FindLPCFLP.SKP_Silk_find_LPC_FLP(NLSF, NLSFInterpCoef_Q2_ptr, psEnc.sPred.prev_NLSFq, psEnc.sCmn.useInterpolatedNLSFs * (1 - psEnc.sCmn.first_frame_after_reset), psEnc.sCmn.predictLPCOrder, LPC_in_pre, psEnc.sCmn.subfr_length + psEnc.sCmn.predictLPCOrder);
        psEncCtrl.sCmn.NLSFInterpCoef_Q2 = NLSFInterpCoef_Q2_ptr[0];
        ProcessNLSFsFLP.SKP_Silk_process_NLSFs_FLP(psEnc, psEncCtrl, NLSF);
        ResidualEnergyFLP.SKP_Silk_residual_energy_FLP(psEncCtrl.ResNrg, LPC_in_pre, psEncCtrl.PredCoef, psEncCtrl.Gains, psEnc.sCmn.subfr_length, psEnc.sCmn.predictLPCOrder);
        System.arraycopy(NLSF, 0, psEnc.sPred.prev_NLSFq, 0, psEnc.sCmn.predictLPCOrder);
    }
}
