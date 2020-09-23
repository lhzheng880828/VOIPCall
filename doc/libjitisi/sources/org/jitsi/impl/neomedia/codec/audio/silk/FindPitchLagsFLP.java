package org.jitsi.impl.neomedia.codec.audio.silk;

public class FindPitchLagsFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!FindPitchLagsFLP.class.desiredAssertionStatus());

    static void SKP_Silk_find_pitch_lags_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control_FLP psEncCtrl, float[] res, float[] x, int x_offset) {
        SKP_Silk_predict_state_FLP psPredSt = psEnc.sPred;
        float[] auto_corr = new float[17];
        float[] A = new float[16];
        float[] refl_coef = new float[16];
        float[] Wsig = new float[864];
        int buf_len = (psEnc.sCmn.frame_length * 2) + psEnc.sCmn.la_pitch;
        if ($assertionsDisabled || buf_len >= psPredSt.pitch_LPC_win_length) {
            int i_djinn;
            float[] x_buf = x;
            int x_buf_offset = x_offset - psEnc.sCmn.frame_length;
            float[] x_buf_ptr = x_buf;
            int x_buf_ptr_offset = (x_buf_offset + buf_len) - psPredSt.pitch_LPC_win_length;
            float[] Wsig_ptr = Wsig;
            ApplySineWindowFLP.SKP_Silk_apply_sine_window_FLP(Wsig_ptr, 0, x_buf_ptr, x_buf_ptr_offset, 1, psEnc.sCmn.la_pitch);
            int Wsig_ptr_offset = 0 + psEnc.sCmn.la_pitch;
            x_buf_ptr_offset += psEnc.sCmn.la_pitch;
            for (i_djinn = 0; i_djinn < psPredSt.pitch_LPC_win_length - (psEnc.sCmn.la_pitch << 1); i_djinn++) {
                Wsig_ptr[Wsig_ptr_offset + i_djinn] = x_buf_ptr[x_buf_ptr_offset + i_djinn];
            }
            ApplySineWindowFLP.SKP_Silk_apply_sine_window_FLP(Wsig_ptr, Wsig_ptr_offset + (psPredSt.pitch_LPC_win_length - (psEnc.sCmn.la_pitch << 1)), x_buf_ptr, x_buf_ptr_offset + (psPredSt.pitch_LPC_win_length - (psEnc.sCmn.la_pitch << 1)), 2, psEnc.sCmn.la_pitch);
            AutocorrelationFLP.SKP_Silk_autocorrelation_FLP(auto_corr, 0, Wsig, 0, psPredSt.pitch_LPC_win_length, psEnc.sCmn.pitchEstimationLPCOrder + 1);
            auto_corr[0] = auto_corr[0] + (auto_corr[0] * 0.001f);
            SchurFLP.SKP_Silk_schur_FLP(refl_coef, 0, auto_corr, 0, psEnc.sCmn.pitchEstimationLPCOrder);
            K2aFLP.SKP_Silk_k2a_FLP(A, refl_coef, psEnc.sCmn.pitchEstimationLPCOrder);
            BwexpanderFLP.SKP_Silk_bwexpander_FLP(A, 0, psEnc.sCmn.pitchEstimationLPCOrder, 0.99f);
            LPCAnalysisFilterFLP.SKP_Silk_LPC_analysis_filter_FLP(res, A, x_buf, x_buf_offset, buf_len, psEnc.sCmn.pitchEstimationLPCOrder);
            for (i_djinn = 0; i_djinn < psEnc.sCmn.pitchEstimationLPCOrder; i_djinn++) {
                res[i_djinn] = 0.0f;
            }
            float thrhld = (((0.5f - (0.004f * ((float) psEnc.sCmn.pitchEstimationLPCOrder))) - (0.1f * ((float) Math.sqrt((double) psEnc.speech_activity)))) + (0.14f * ((float) psEnc.sCmn.prev_sigtype))) - (0.12f * psEncCtrl.input_tilt);
            int[] lagIndex_djinnaddress = new int[]{psEncCtrl.sCmn.lagIndex};
            int[] contourIndex_djinnaddress = new int[]{psEncCtrl.sCmn.contourIndex};
            float[] LTPCorr_djinnaddress = new float[]{psEnc.LTPCorr};
            psEncCtrl.sCmn.sigtype = PitchAnalysisCoreFLP.SKP_Silk_pitch_analysis_core_FLP(res, psEncCtrl.sCmn.pitchL, lagIndex_djinnaddress, contourIndex_djinnaddress, LTPCorr_djinnaddress, psEnc.sCmn.prevLag, psEnc.pitchEstimationThreshold, thrhld, psEnc.sCmn.fs_kHz, psEnc.sCmn.pitchEstimationComplexity);
            psEncCtrl.sCmn.lagIndex = lagIndex_djinnaddress[0];
            psEncCtrl.sCmn.contourIndex = contourIndex_djinnaddress[0];
            psEnc.LTPCorr = LTPCorr_djinnaddress[0];
            return;
        }
        throw new AssertionError();
    }
}
