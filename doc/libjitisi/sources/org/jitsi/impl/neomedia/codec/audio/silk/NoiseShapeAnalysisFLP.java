package org.jitsi.impl.neomedia.codec.audio.silk;

public class NoiseShapeAnalysisFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!NoiseShapeAnalysisFLP.class.desiredAssertionStatus());

    static void SKP_Silk_noise_shape_analysis_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control_FLP psEncCtrl, float[] pitch_res, int pitch_res_offset, float[] x, int x_offset) {
        int k;
        float[] fArr;
        float Tilt;
        float HarmShapeGain;
        SKP_Silk_shape_state_FLP psShapeSt = psEnc.sShape;
        float pre_nrg = 0.0f;
        float[] x_windowed = new float[360];
        float[] auto_corr = new float[17];
        float[] x_ptr = x;
        int x_ptr_offset = ((psEnc.sCmn.la_shape + x_offset) - (psEnc.sCmn.fs_kHz * 15)) + psEnc.sCmn.subfr_length;
        psEncCtrl.current_SNR_dB = psEnc.SNR_dB - (0.05f * psEnc.BufferedInChannel_ms);
        if (psEnc.speech_activity > DefineFLP.LBRR_SPEECH_ACTIVITY_THRES) {
            psEncCtrl.current_SNR_dB -= psEnc.inBandFEC_SNR_comp;
        }
        psEncCtrl.input_quality = 0.5f * (psEncCtrl.input_quality_bands[0] + psEncCtrl.input_quality_bands[1]);
        psEncCtrl.coding_quality = SigProcFLP.SKP_sigmoid(0.25f * (psEncCtrl.current_SNR_dB - 18.0f));
        float b = 1.0f - psEnc.speech_activity;
        float SNR_adj_dB = psEncCtrl.current_SNR_dB - ((((3.0f * psEncCtrl.coding_quality) * (0.5f + (0.5f * psEncCtrl.input_quality))) * b) * b);
        if (psEncCtrl.sCmn.sigtype == 0) {
            SNR_adj_dB += 2.0f * psEnc.LTPCorr;
        } else {
            SNR_adj_dB += ((-0.4f * psEncCtrl.current_SNR_dB) + 6.0f) * (1.0f - psEncCtrl.input_quality);
        }
        if (psEncCtrl.sCmn.sigtype == 0) {
            psEncCtrl.sCmn.QuantOffsetType = 0;
            psEncCtrl.sparseness = 0.0f;
        } else {
            int nSamples = psEnc.sCmn.fs_kHz * 2;
            float energy_variation = 0.0f;
            float log_energy_prev = 0.0f;
            float[] pitch_res_ptr = pitch_res;
            int pitch_res_ptr_offset = pitch_res_offset;
            for (k = 0; k < 10; k++) {
                float log_energy = MainFLP.SKP_Silk_log2((double) (((float) nSamples) + ((float) EnergyFLP.SKP_Silk_energy_FLP(pitch_res_ptr, pitch_res_ptr_offset, nSamples))));
                if (k > 0) {
                    energy_variation += Math.abs(log_energy - log_energy_prev);
                }
                log_energy_prev = log_energy;
                pitch_res_ptr_offset += nSamples;
            }
            psEncCtrl.sparseness = SigProcFLP.SKP_sigmoid(0.4f * (energy_variation - 5.0f));
            if (psEncCtrl.sparseness > 0.75f) {
                psEncCtrl.sCmn.QuantOffsetType = 0;
            } else {
                psEncCtrl.sCmn.QuantOffsetType = 1;
            }
            SNR_adj_dB += 2.0f * (psEncCtrl.sparseness - 0.5f);
        }
        float delta = 0.01f * (1.0f - (0.75f * psEncCtrl.coding_quality));
        float BWExp1 = 0.94f - delta;
        float BWExp2 = 0.94f + delta;
        if (psEnc.sCmn.fs_kHz == 24) {
            BWExp1 = 1.0f - ((1.0f - BWExp1) * 1.0f);
            BWExp2 = 1.0f - ((1.0f - BWExp2) * 1.0f);
        }
        BWExp1 /= BWExp2;
        for (k = 0; k < 4; k++) {
            ApplySineWindowFLP.SKP_Silk_apply_sine_window_FLP(x_windowed, 0, x_ptr, x_ptr_offset, 0, psEnc.sCmn.fs_kHz * 15);
            x_ptr_offset += psEnc.sCmn.subfr_length;
            AutocorrelationFLP.SKP_Silk_autocorrelation_FLP(auto_corr, 0, x_windowed, 0, psEnc.sCmn.fs_kHz * 15, psEnc.sCmn.shapingLPCOrder + 1);
            auto_corr[0] = auto_corr[0] + (auto_corr[0] * 4.7684E-5f);
            float nrg = LevinsondurbinFLP.SKP_Silk_levinsondurbin_FLP(psEncCtrl.AR2, k * 16, auto_corr, psEnc.sCmn.shapingLPCOrder);
            BwexpanderFLP.SKP_Silk_bwexpander_FLP(psEncCtrl.AR2, k * 16, psEnc.sCmn.shapingLPCOrder, BWExp2);
            LPC_fit_int16(psEncCtrl.AR2, k * 16, 1.0f, psEnc.sCmn.shapingLPCOrder, 3.999f);
            for (int i_djinn = 0; i_djinn < psEnc.sCmn.shapingLPCOrder; i_djinn++) {
                psEncCtrl.AR1[(k * 16) + i_djinn] = psEncCtrl.AR2[(k * 16) + i_djinn];
            }
            BwexpanderFLP.SKP_Silk_bwexpander_FLP(psEncCtrl.AR1, k * 16, psEnc.sCmn.shapingLPCOrder, BWExp1);
            psEncCtrl.Gains[k] = (float) Math.sqrt((double) (nrg + (1.526E-5f * auto_corr[0])));
            float[] pre_nrg_djinnaddress = new float[]{pre_nrg};
            LPCInvPredGainFLP.SKP_Silk_LPC_inverse_pred_gain_FLP(pre_nrg_djinnaddress, psEncCtrl.AR2, k * 16, psEnc.sCmn.shapingLPCOrder);
            pre_nrg = pre_nrg_djinnaddress[0];
            float[] nrg_djinnaddress = new float[]{nrg};
            LPCInvPredGainFLP.SKP_Silk_LPC_inverse_pred_gain_FLP(nrg_djinnaddress, psEncCtrl.AR1, k * 16, psEnc.sCmn.shapingLPCOrder);
            psEncCtrl.GainsPre[k] = (float) Math.sqrt((double) (pre_nrg / nrg_djinnaddress[0]));
        }
        float gain_mult = (float) Math.pow(2.0d, (double) (-0.16f * SNR_adj_dB));
        float gain_add = ((float) Math.pow(2.0d, 0.6399999856948853d)) + (((float) Math.pow(2.0d, -8.0d)) * psEnc.avgGain);
        for (k = 0; k < 4; k++) {
            fArr = psEncCtrl.Gains;
            fArr[k] = fArr[k] * gain_mult;
            fArr = psEncCtrl.Gains;
            fArr[k] = fArr[k] + gain_add;
            psEnc.avgGain += (psEnc.speech_activity * 0.001f) * (psEncCtrl.Gains[k] - psEnc.avgGain);
        }
        gain_mult = 1.04f + (psEncCtrl.coding_quality * 0.06f);
        if (psEncCtrl.input_tilt <= 0.0f && psEncCtrl.sCmn.sigtype == 1) {
            float essStrength = ((-psEncCtrl.input_tilt) * psEnc.speech_activity) * (1.0f - psEncCtrl.sparseness);
            if (psEnc.sCmn.fs_kHz == 24) {
                gain_mult *= (float) Math.pow(2.0d, (double) (-0.32f * essStrength));
            } else if (psEnc.sCmn.fs_kHz == 16) {
                gain_mult *= (float) Math.pow(2.0d, (double) (-0.16f * essStrength));
            } else if (!($assertionsDisabled || psEnc.sCmn.fs_kHz == 12 || psEnc.sCmn.fs_kHz == 8)) {
                throw new AssertionError();
            }
        }
        for (k = 0; k < 4; k++) {
            fArr = psEncCtrl.GainsPre;
            fArr[k] = fArr[k] * gain_mult;
        }
        float strength = 3.0f * (1.0f + (0.5f * (psEncCtrl.input_quality_bands[0] - 1.0f)));
        if (psEncCtrl.sCmn.sigtype == 0) {
            for (k = 0; k < 4; k++) {
                b = (0.2f / ((float) psEnc.sCmn.fs_kHz)) + (3.0f / ((float) psEncCtrl.sCmn.pitchL[k]));
                psEncCtrl.LF_MA_shp[k] = -1.0f + b;
                psEncCtrl.LF_AR_shp[k] = (1.0f - b) - (b * strength);
            }
            Tilt = -0.3f - (0.315f * psEnc.speech_activity);
        } else {
            b = 1.3f / ((float) psEnc.sCmn.fs_kHz);
            psEncCtrl.LF_MA_shp[0] = -1.0f + b;
            psEncCtrl.LF_AR_shp[0] = (1.0f - b) - ((b * strength) * 0.6f);
            for (k = 1; k < 4; k++) {
                psEncCtrl.LF_MA_shp[k] = psEncCtrl.LF_MA_shp[k - 1];
                psEncCtrl.LF_AR_shp[k] = psEncCtrl.LF_AR_shp[k - 1];
            }
            Tilt = -0.3f;
        }
        float HarmBoost = ((0.1f * (1.0f - psEncCtrl.coding_quality)) * psEnc.LTPCorr) + (0.1f * (1.0f - psEncCtrl.input_quality));
        if (psEncCtrl.sCmn.sigtype == 0) {
            HarmShapeGain = (0.3f + (0.2f * (1.0f - ((1.0f - psEncCtrl.coding_quality) * psEncCtrl.input_quality)))) * ((float) Math.sqrt((double) psEnc.LTPCorr));
        } else {
            HarmShapeGain = 0.0f;
        }
        for (k = 0; k < 4; k++) {
            psShapeSt.HarmBoost_smth += 0.4f * (HarmBoost - psShapeSt.HarmBoost_smth);
            psEncCtrl.HarmBoost[k] = psShapeSt.HarmBoost_smth;
            psShapeSt.HarmShapeGain_smth += 0.4f * (HarmShapeGain - psShapeSt.HarmShapeGain_smth);
            psEncCtrl.HarmShapeGain[k] = psShapeSt.HarmShapeGain_smth;
            psShapeSt.Tilt_smth += 0.4f * (Tilt - psShapeSt.Tilt_smth);
            psEncCtrl.Tilt[k] = psShapeSt.Tilt_smth;
        }
    }

    static void LPC_fit_int16(float[] a, int a_offset, float bwe, int L, float maxVal) {
        int i;
        int idx = 0;
        float[] invGain = new float[1];
        BwexpanderFLP.SKP_Silk_bwexpander_FLP(a, a_offset, L, bwe);
        int k = 0;
        while (k < 1000) {
            float maxabs = -1.0f;
            for (i = 0; i < L; i++) {
                float absval = Math.abs(a[a_offset + i]);
                if (absval > maxabs) {
                    maxabs = absval;
                    idx = i;
                }
            }
            if (maxabs < maxVal) {
                break;
            }
            BwexpanderFLP.SKP_Silk_bwexpander_FLP(a, a_offset, L, 0.995f * (1.0f - ((1.0f - (maxVal / maxabs)) / ((float) (idx + 1)))));
            k++;
        }
        if (k != 1000 || $assertionsDisabled) {
            k = 0;
            while (k < 1000 && LPCInvPredGainFLP.SKP_Silk_LPC_inverse_pred_gain_FLP(invGain, a, a_offset, L) == 1) {
                BwexpanderFLP.SKP_Silk_bwexpander_FLP(a, a_offset, L, 0.997f);
                k++;
            }
            if (k != 1000) {
                return;
            }
            if ($assertionsDisabled) {
                for (i = 0; i < L; i++) {
                    a[i] = 0.0f;
                }
                return;
            }
            throw new AssertionError();
        }
        throw new AssertionError();
    }
}
