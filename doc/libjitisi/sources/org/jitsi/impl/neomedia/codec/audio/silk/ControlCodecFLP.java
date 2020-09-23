package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;
import javax.media.Buffer;

public class ControlCodecFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!ControlCodecFLP.class.desiredAssertionStatus());

    static int SKP_Silk_control_encoder_FLP(SKP_Silk_encoder_state_FLP psEnc, int API_fs_Hz, int max_internal_fs_kHz, int PacketSize_ms, int TargetRate_bps, int PacketLoss_perc, int INBandFEC_enabled, int DTX_enabled, int InputFramesize_ms, int Complexity) {
        int ret = 0;
        int fs_kHz = psEnc.sCmn.fs_kHz;
        if (API_fs_Hz == 8000 || fs_kHz == 0 || API_fs_Hz < fs_kHz * 1000 || fs_kHz > max_internal_fs_kHz) {
            fs_kHz = Math.min(API_fs_Hz / 1000, max_internal_fs_kHz);
        } else {
            SKP_Silk_encoder_state sKP_Silk_encoder_state = psEnc.sCmn;
            sKP_Silk_encoder_state.bitrateDiff += (TargetRate_bps - psEnc.sCmn.bitrate_threshold_down) * InputFramesize_ms;
            psEnc.sCmn.bitrateDiff = Math.min(psEnc.sCmn.bitrateDiff, 0);
            if (psEnc.speech_activity < 0.5f && psEnc.sCmn.nFramesInPayloadBuf == 0) {
                if (psEnc.sCmn.sLP.transition_frame_no == 0 && (psEnc.sCmn.bitrateDiff <= -30000000 || psEnc.sCmn.sSWBdetect.WB_detected * psEnc.sCmn.fs_kHz == 24)) {
                    psEnc.sCmn.sLP.transition_frame_no = 1;
                    psEnc.sCmn.sLP.mode = 0;
                } else if (psEnc.sCmn.sLP.transition_frame_no >= 128 && psEnc.sCmn.sLP.mode == 0) {
                    psEnc.sCmn.sLP.transition_frame_no = 0;
                    psEnc.sCmn.bitrateDiff = 0;
                    if (psEnc.sCmn.fs_kHz == 24) {
                        fs_kHz = 16;
                    } else if (psEnc.sCmn.fs_kHz == 16) {
                        fs_kHz = 12;
                    } else if ($assertionsDisabled || psEnc.sCmn.fs_kHz == 12) {
                        fs_kHz = 8;
                    } else {
                        throw new AssertionError();
                    }
                }
                if (psEnc.sCmn.fs_kHz * 1000 < API_fs_Hz && TargetRate_bps >= psEnc.sCmn.bitrate_threshold_up && psEnc.sCmn.sSWBdetect.WB_detected * psEnc.sCmn.fs_kHz != 16 && (((psEnc.sCmn.fs_kHz == 16 && max_internal_fs_kHz >= 24) || ((psEnc.sCmn.fs_kHz == 12 && max_internal_fs_kHz >= 16) || (psEnc.sCmn.fs_kHz == 8 && max_internal_fs_kHz >= 12))) && psEnc.sCmn.sLP.transition_frame_no == 0)) {
                    psEnc.sCmn.sLP.mode = 1;
                    psEnc.sCmn.bitrateDiff = 0;
                    if (psEnc.sCmn.fs_kHz == 8) {
                        fs_kHz = 12;
                    } else if (psEnc.sCmn.fs_kHz == 12) {
                        fs_kHz = 16;
                    } else if ($assertionsDisabled || psEnc.sCmn.fs_kHz == 16) {
                        fs_kHz = 24;
                    } else {
                        throw new AssertionError();
                    }
                }
            }
        }
        if (psEnc.sCmn.sLP.mode == 1 && psEnc.sCmn.sLP.transition_frame_no >= 256 && psEnc.speech_activity < 0.5f && psEnc.sCmn.nFramesInPayloadBuf == 0) {
            psEnc.sCmn.sLP.transition_frame_no = 0;
            Arrays.fill(psEnc.sCmn.sLP.In_LP_State, 0, 2, 0);
        }
        if (!(psEnc.sCmn.fs_kHz == fs_kHz && psEnc.sCmn.prev_API_fs_Hz == API_fs_Hz)) {
            short[] x_buf_API_fs_Hz = new short[6480];
            short[] x_bufFIX = new short[1080];
            int nSamples_temp = (psEnc.sCmn.frame_length * 2) + psEnc.sCmn.la_shape;
            SigProcFLP.SKP_float2short_array(x_bufFIX, 0, psEnc.x_buf, 0, 1080);
            if (fs_kHz * 1000 >= API_fs_Hz || psEnc.sCmn.fs_kHz == 0) {
                System.arraycopy(x_bufFIX, 0, x_buf_API_fs_Hz, 0, nSamples_temp);
            } else {
                SKP_Silk_resampler_state_struct temp_resampler_state = new SKP_Silk_resampler_state_struct();
                ret = (0 + Resampler.SKP_Silk_resampler_init(temp_resampler_state, psEnc.sCmn.fs_kHz * 1000, API_fs_Hz)) + Resampler.SKP_Silk_resampler(temp_resampler_state, x_buf_API_fs_Hz, 0, x_bufFIX, 0, nSamples_temp);
                nSamples_temp = (nSamples_temp * API_fs_Hz) / (psEnc.sCmn.fs_kHz * 1000);
                ret += Resampler.SKP_Silk_resampler_init(psEnc.sCmn.resampler_state, API_fs_Hz, fs_kHz * 1000);
            }
            if (fs_kHz * 1000 != API_fs_Hz) {
                ret += Resampler.SKP_Silk_resampler(psEnc.sCmn.resampler_state, x_bufFIX, 0, x_buf_API_fs_Hz, 0, nSamples_temp);
            }
            SigProcFLP.SKP_short2float_array(psEnc.x_buf, 0, x_bufFIX, 0, 1080);
        }
        psEnc.sCmn.prev_API_fs_Hz = API_fs_Hz;
        if (psEnc.sCmn.fs_kHz != fs_kHz) {
            psEnc.sShape.memZero();
            psEnc.sPrefilt.memZero();
            psEnc.sNSQ.memZero();
            psEnc.sPred.memZero();
            Arrays.fill(psEnc.sNSQ.xq, 0, 960, (short) 0);
            Arrays.fill(psEnc.sNSQ_LBRR.xq, (short) 0);
            for (int i = 0; i < 2; i++) {
                psEnc.sCmn.LBRR_buffer[i].memZero();
            }
            Arrays.fill(psEnc.sCmn.sLP.In_LP_State, 0, 2, 0);
            if (psEnc.sCmn.sLP.mode == 1) {
                psEnc.sCmn.sLP.transition_frame_no = 1;
            } else {
                psEnc.sCmn.sLP.transition_frame_no = 0;
            }
            psEnc.sCmn.inputBufIx = 0;
            psEnc.sCmn.nFramesInPayloadBuf = 0;
            psEnc.sCmn.nBytesInPayloadBuf = 0;
            psEnc.sCmn.oldest_LBRR_idx = 0;
            psEnc.sCmn.TargetRate_bps = 0;
            Arrays.fill(psEnc.sPred.prev_NLSFq, 0, 16, 0.0f);
            psEnc.sCmn.prevLag = 100;
            psEnc.sCmn.prev_sigtype = 1;
            psEnc.sCmn.first_frame_after_reset = 1;
            psEnc.sPrefilt.lagPrev = 100;
            psEnc.sShape.LastGainIndex = 1;
            psEnc.sNSQ.lagPrev = 100;
            psEnc.sNSQ.prev_inv_gain_Q16 = Buffer.FLAG_SKIP_FEC;
            psEnc.sNSQ_LBRR.prev_inv_gain_Q16 = Buffer.FLAG_SKIP_FEC;
            psEnc.sCmn.fs_kHz = fs_kHz;
            if (psEnc.sCmn.fs_kHz == 8) {
                psEnc.sCmn.predictLPCOrder = 10;
                psEnc.sCmn.psNLSF_CB[0] = TablesNLSFCB010.SKP_Silk_NLSF_CB0_10;
                psEnc.sCmn.psNLSF_CB[1] = TablesNLSFCB110.SKP_Silk_NLSF_CB1_10;
                psEnc.psNLSF_CB_FLP[0] = TablesNLSFCB010FLP.SKP_Silk_NLSF_CB0_10_FLP;
                psEnc.psNLSF_CB_FLP[1] = TablesNLSFCB110FLP.SKP_Silk_NLSF_CB1_10_FLP;
            } else {
                psEnc.sCmn.predictLPCOrder = 16;
                psEnc.sCmn.psNLSF_CB[0] = TablesNLSFCB016.SKP_Silk_NLSF_CB0_16;
                psEnc.sCmn.psNLSF_CB[1] = TablesNLSFCB116.SKP_Silk_NLSF_CB1_16;
                psEnc.psNLSF_CB_FLP[0] = TablesNLSFCB016FLP.SKP_Silk_NLSF_CB0_16_FLP;
                psEnc.psNLSF_CB_FLP[1] = TablesNLSFCB116FLP.SKP_Silk_NLSF_CB1_16_FLP;
            }
            psEnc.sCmn.frame_length = fs_kHz * 20;
            psEnc.sCmn.subfr_length = psEnc.sCmn.frame_length / 4;
            psEnc.sCmn.la_pitch = fs_kHz * 3;
            psEnc.sCmn.la_shape = fs_kHz * 5;
            psEnc.sPred.min_pitch_lag = fs_kHz * 3;
            psEnc.sPred.max_pitch_lag = fs_kHz * 18;
            psEnc.sPred.pitch_LPC_win_length = fs_kHz * 36;
            if (psEnc.sCmn.fs_kHz == 24) {
                psEnc.mu_LTP = 0.016f;
                psEnc.sCmn.bitrate_threshold_up = Integer.MAX_VALUE;
                psEnc.sCmn.bitrate_threshold_down = 26000;
            } else if (psEnc.sCmn.fs_kHz == 16) {
                psEnc.mu_LTP = 0.02f;
                psEnc.sCmn.bitrate_threshold_up = 32000;
                psEnc.sCmn.bitrate_threshold_down = 15000;
            } else if (psEnc.sCmn.fs_kHz == 12) {
                psEnc.mu_LTP = 0.025f;
                psEnc.sCmn.bitrate_threshold_up = 20000;
                psEnc.sCmn.bitrate_threshold_down = 10000;
            } else {
                psEnc.mu_LTP = 0.03f;
                psEnc.sCmn.bitrate_threshold_up = 14000;
                psEnc.sCmn.bitrate_threshold_down = 0;
            }
            psEnc.sCmn.fs_kHz_changed = 1;
            if (!($assertionsDisabled || psEnc.sCmn.subfr_length * 4 == psEnc.sCmn.frame_length)) {
                throw new AssertionError();
            }
        }
        if (Complexity == 0) {
            psEnc.sCmn.Complexity = 0;
            psEnc.sCmn.pitchEstimationComplexity = 0;
            psEnc.pitchEstimationThreshold = 0.8f;
            psEnc.sCmn.pitchEstimationLPCOrder = 8;
            psEnc.sCmn.shapingLPCOrder = 12;
            psEnc.sCmn.nStatesDelayedDecision = 1;
            psEnc.noiseShapingQuantizerCB = new NSQImplNSQ();
            psEnc.sCmn.useInterpolatedNLSFs = 0;
            psEnc.sCmn.LTPQuantLowComplexity = 1;
            psEnc.sCmn.NLSF_MSVQ_Survivors = 2;
        } else if (Complexity == 1) {
            psEnc.sCmn.Complexity = 1;
            psEnc.sCmn.pitchEstimationComplexity = 1;
            psEnc.pitchEstimationThreshold = 0.75f;
            psEnc.sCmn.pitchEstimationLPCOrder = 12;
            psEnc.sCmn.shapingLPCOrder = 16;
            psEnc.sCmn.nStatesDelayedDecision = 2;
            psEnc.noiseShapingQuantizerCB = new NSQImplNSQDelDec();
            psEnc.sCmn.useInterpolatedNLSFs = 0;
            psEnc.sCmn.LTPQuantLowComplexity = 0;
            psEnc.sCmn.NLSF_MSVQ_Survivors = 4;
        } else if (Complexity == 2) {
            psEnc.sCmn.Complexity = 2;
            psEnc.sCmn.pitchEstimationComplexity = 2;
            psEnc.pitchEstimationThreshold = 0.7f;
            psEnc.sCmn.pitchEstimationLPCOrder = 16;
            psEnc.sCmn.shapingLPCOrder = 16;
            psEnc.sCmn.nStatesDelayedDecision = 4;
            psEnc.noiseShapingQuantizerCB = new NSQImplNSQDelDec();
            psEnc.sCmn.useInterpolatedNLSFs = 1;
            psEnc.sCmn.LTPQuantLowComplexity = 0;
            psEnc.sCmn.NLSF_MSVQ_Survivors = 16;
        } else {
            ret = -6;
        }
        psEnc.sCmn.pitchEstimationLPCOrder = Math.min(psEnc.sCmn.pitchEstimationLPCOrder, psEnc.sCmn.predictLPCOrder);
        if (!$assertionsDisabled && psEnc.sCmn.pitchEstimationLPCOrder > 16) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && psEnc.sCmn.shapingLPCOrder > 16) {
            throw new AssertionError();
        } else if ($assertionsDisabled || psEnc.sCmn.nStatesDelayedDecision <= 4) {
            TargetRate_bps = Math.min(TargetRate_bps, 100000);
            if (psEnc.sCmn.fs_kHz == 8) {
                TargetRate_bps = Math.max(TargetRate_bps, 5000);
            } else if (psEnc.sCmn.fs_kHz == 12) {
                TargetRate_bps = Math.max(TargetRate_bps, 7000);
            } else if (psEnc.sCmn.fs_kHz == 16) {
                TargetRate_bps = Math.max(TargetRate_bps, 8000);
            } else {
                TargetRate_bps = Math.max(TargetRate_bps, 20000);
            }
            if (TargetRate_bps != psEnc.sCmn.TargetRate_bps) {
                int[] rateTable;
                psEnc.sCmn.TargetRate_bps = TargetRate_bps;
                if (psEnc.sCmn.fs_kHz == 8) {
                    rateTable = TablesOther.TargetRate_table_NB;
                } else if (psEnc.sCmn.fs_kHz == 12) {
                    rateTable = TablesOther.TargetRate_table_MB;
                } else if (psEnc.sCmn.fs_kHz == 16) {
                    rateTable = TablesOther.TargetRate_table_WB;
                } else {
                    rateTable = TablesOther.TargetRate_table_SWB;
                }
                for (int k = 1; k < 8; k++) {
                    if (TargetRate_bps < rateTable[k]) {
                        psEnc.SNR_dB = 0.5f * (((float) TablesOther.SNR_table_Q1[k - 1]) + (((float) (TablesOther.SNR_table_Q1[k] - TablesOther.SNR_table_Q1[k - 1])) * (((float) (TargetRate_bps - rateTable[k - 1])) / ((float) (rateTable[k] - rateTable[k - 1])))));
                        break;
                    }
                }
            }
            if (PacketSize_ms != 20 && PacketSize_ms != 40 && PacketSize_ms != 60 && PacketSize_ms != 80 && PacketSize_ms != 100) {
                ret = -3;
            } else if (PacketSize_ms != psEnc.sCmn.PacketSize_ms) {
                psEnc.sCmn.PacketSize_ms = PacketSize_ms;
                LBRRReset.SKP_Silk_LBRR_reset(psEnc.sCmn);
            }
            if (PacketLoss_perc < 0 || PacketLoss_perc > 100) {
                ret = -5;
            }
            psEnc.sCmn.PacketLoss_perc = PacketLoss_perc;
            if (INBandFEC_enabled < 0 || INBandFEC_enabled > 1) {
                ret = -7;
            }
            if (psEnc.sCmn.nFramesInPayloadBuf == 0) {
                int LBRRRate_thres_bps;
                psEnc.sCmn.LBRR_enabled = INBandFEC_enabled;
                if (psEnc.sCmn.fs_kHz == 8) {
                    LBRRRate_thres_bps = 9000;
                } else if (psEnc.sCmn.fs_kHz == 12) {
                    LBRRRate_thres_bps = 12000;
                } else if (psEnc.sCmn.fs_kHz == 16) {
                    LBRRRate_thres_bps = 15000;
                } else {
                    LBRRRate_thres_bps = 18000;
                }
                if (psEnc.sCmn.TargetRate_bps >= LBRRRate_thres_bps) {
                    psEnc.sCmn.LBRR_GainIncreases = Math.max(8 - (psEnc.sCmn.PacketLoss_perc >> 1), 0);
                    if (psEnc.sCmn.LBRR_enabled == 0 || psEnc.sCmn.PacketLoss_perc <= 2) {
                        psEnc.inBandFEC_SNR_comp = 0.0f;
                        psEnc.sCmn.LBRR_enabled = 0;
                    } else {
                        psEnc.inBandFEC_SNR_comp = 6.0f - (0.5f * ((float) psEnc.sCmn.LBRR_GainIncreases));
                    }
                } else {
                    psEnc.inBandFEC_SNR_comp = 0.0f;
                    psEnc.sCmn.LBRR_enabled = 0;
                }
            }
            if (DTX_enabled < 0 || DTX_enabled > 1) {
                ret = -8;
            }
            psEnc.sCmn.useDTX = DTX_enabled;
            return ret;
        } else {
            throw new AssertionError();
        }
    }

    static void SKP_Silk_LBRR_ctrl_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control psEncCtrl) {
        if (psEnc.sCmn.LBRR_enabled != 0) {
            int LBRR_usage = 0;
            if (psEnc.speech_activity > DefineFLP.LBRR_SPEECH_ACTIVITY_THRES && psEnc.sCmn.PacketLoss_perc > 2) {
                LBRR_usage = 1;
            }
            psEncCtrl.LBRR_usage = LBRR_usage;
            return;
        }
        psEncCtrl.LBRR_usage = 0;
    }
}
