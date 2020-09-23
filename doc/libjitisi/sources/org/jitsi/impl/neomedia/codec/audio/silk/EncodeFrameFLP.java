package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class EncodeFrameFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!EncodeFrameFLP.class.desiredAssertionStatus());
    static int frame_cnt = 0;

    static int SKP_Silk_encode_frame_FLP(SKP_Silk_encoder_state_FLP psEnc, byte[] pCode, int pCode_offset, short[] pnBytesOut, short[] pIn, int pIn_offset) {
        SKP_Silk_encoder_state sKP_Silk_encoder_state;
        SKP_Silk_encoder_control_FLP sEncCtrl = new SKP_Silk_encoder_control_FLP();
        int[] nBytes = new int[1];
        int ret = 0;
        short[] pIn_HP = new short[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        short[] pIn_HP_LP = new short[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        float[] xfw = new float[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        float[] res_pitch = new float[1032];
        Object LBRRpayload = new byte[1024];
        short[] nBytesLBRR = new short[1];
        SKP_Silk_encoder_control sKP_Silk_encoder_control = sEncCtrl.sCmn;
        SKP_Silk_encoder_state sKP_Silk_encoder_state2 = psEnc.sCmn;
        int i = sKP_Silk_encoder_state2.frameCounter;
        sKP_Silk_encoder_state2.frameCounter = i + 1;
        sKP_Silk_encoder_control.Seed = i & 3;
        float[] x_frame = psEnc.x_buf;
        int x_frame_offset = psEnc.x_buf_offset + psEnc.sCmn.frame_length;
        float[] res_pitch_frame = res_pitch;
        int res_pitch_frame_offset = psEnc.sCmn.frame_length;
        WrappersFLP.SKP_Silk_VAD_FLP(psEnc, sEncCtrl, pIn, pIn_offset);
        HPVariableCutoffFLP.SKP_Silk_HP_variable_cutoff_FLP(psEnc, sEncCtrl, pIn_HP, 0, pIn, pIn_offset);
        LPVariableCutoff.SKP_Silk_LP_variable_cutoff(psEnc.sCmn.sLP, pIn_HP_LP, 0, pIn_HP, 0, psEnc.sCmn.frame_length);
        SigProcFLP.SKP_short2float_array(x_frame, psEnc.sCmn.la_shape + x_frame_offset, pIn_HP_LP, 0, psEnc.sCmn.frame_length);
        for (int k = 0; k < 8; k++) {
            int i2 = (psEnc.sCmn.la_shape + x_frame_offset) + ((psEnc.sCmn.frame_length >> 3) * k);
            x_frame[i2] = x_frame[i2] + (((float) (1 - (k & 2))) * 1.0E-6f);
        }
        FindPitchLagsFLP.SKP_Silk_find_pitch_lags_FLP(psEnc, sEncCtrl, res_pitch, x_frame, x_frame_offset);
        NoiseShapeAnalysisFLP.SKP_Silk_noise_shape_analysis_FLP(psEnc, sEncCtrl, res_pitch_frame, res_pitch_frame_offset, x_frame, x_frame_offset);
        PrefilterFLP.SKP_Silk_prefilter_FLP(psEnc, sEncCtrl, xfw, x_frame, x_frame_offset);
        FindPredCoefsFLP.SKP_Silk_find_pred_coefs_FLP(psEnc, sEncCtrl, res_pitch);
        ProcessGainsFLP.SKP_Silk_process_gains_FLP(psEnc, sEncCtrl);
        nBytesLBRR[0] = (short) 1024;
        SKP_Silk_LBRR_encode_FLP(psEnc, sEncCtrl, LBRRpayload, nBytesLBRR, xfw);
        WrappersFLP.SKP_Silk_NSQ_wrapper_FLP(psEnc, sEncCtrl, xfw, 0, psEnc.sCmn.q, 0, 0);
        if (psEnc.speech_activity < 0.1f) {
            psEnc.sCmn.vadFlag = 0;
            sKP_Silk_encoder_state = psEnc.sCmn;
            sKP_Silk_encoder_state.noSpeechCounter++;
            if (psEnc.sCmn.noSpeechCounter > 5) {
                psEnc.sCmn.inDTX = 1;
            }
            if (psEnc.sCmn.noSpeechCounter > 20) {
                psEnc.sCmn.noSpeechCounter = 0;
                psEnc.sCmn.inDTX = 0;
            }
        } else {
            psEnc.sCmn.noSpeechCounter = 0;
            psEnc.sCmn.inDTX = 0;
            psEnc.sCmn.vadFlag = 1;
        }
        if (psEnc.sCmn.nFramesInPayloadBuf == 0) {
            RangeCoder.SKP_Silk_range_enc_init(psEnc.sCmn.sRC);
            psEnc.sCmn.nBytesInPayloadBuf = 0;
        }
        EncodeParameters.SKP_Silk_encode_parameters(psEnc.sCmn, sEncCtrl.sCmn, psEnc.sCmn.sRC, psEnc.sCmn.q);
        int[] FrameTermination_CDF = TablesOther.SKP_Silk_FrameTermination_CDF;
        System.arraycopy(psEnc.x_buf, psEnc.x_buf_offset + psEnc.sCmn.frame_length, psEnc.x_buf, psEnc.x_buf_offset, psEnc.sCmn.frame_length + psEnc.sCmn.la_shape);
        psEnc.sCmn.prev_sigtype = sEncCtrl.sCmn.sigtype;
        psEnc.sCmn.prevLag = sEncCtrl.sCmn.pitchL[3];
        psEnc.sCmn.first_frame_after_reset = 0;
        if (psEnc.sCmn.sRC.error != 0) {
            psEnc.sCmn.nFramesInPayloadBuf = 0;
        } else {
            sKP_Silk_encoder_state = psEnc.sCmn;
            sKP_Silk_encoder_state.nFramesInPayloadBuf++;
        }
        if (psEnc.sCmn.nFramesInPayloadBuf * 20 >= psEnc.sCmn.PacketSize_ms) {
            int LBRR_idx = (psEnc.sCmn.oldest_LBRR_idx + 1) & 1;
            int frame_terminator = 0;
            if (psEnc.sCmn.LBRR_buffer[LBRR_idx].usage == 1) {
                frame_terminator = 2;
            }
            if (psEnc.sCmn.LBRR_buffer[psEnc.sCmn.oldest_LBRR_idx].usage == 2) {
                frame_terminator = 3;
                LBRR_idx = psEnc.sCmn.oldest_LBRR_idx;
            }
            RangeCoder.SKP_Silk_range_encoder(psEnc.sCmn.sRC, frame_terminator, FrameTermination_CDF, 0);
            RangeCoder.SKP_Silk_range_coder_get_length(psEnc.sCmn.sRC, nBytes);
            if (pnBytesOut[0] >= nBytes[0]) {
                RangeCoder.SKP_Silk_range_enc_wrap_up(psEnc.sCmn.sRC);
                System.arraycopy(psEnc.sCmn.sRC.buffer, 0, pCode, pCode_offset, nBytes[0]);
                if (frame_terminator > 1 && pnBytesOut[0] >= nBytes[0] + psEnc.sCmn.LBRR_buffer[LBRR_idx].nBytes) {
                    System.arraycopy(psEnc.sCmn.LBRR_buffer[LBRR_idx].payload, 0, pCode, nBytes[0] + pCode_offset, psEnc.sCmn.LBRR_buffer[LBRR_idx].nBytes);
                    nBytes[0] = nBytes[0] + psEnc.sCmn.LBRR_buffer[LBRR_idx].nBytes;
                }
                pnBytesOut[0] = (short) nBytes[0];
                System.arraycopy(LBRRpayload, 0, psEnc.sCmn.LBRR_buffer[psEnc.sCmn.oldest_LBRR_idx].payload, 0, nBytesLBRR[0]);
                psEnc.sCmn.LBRR_buffer[psEnc.sCmn.oldest_LBRR_idx].nBytes = nBytesLBRR[0];
                psEnc.sCmn.LBRR_buffer[psEnc.sCmn.oldest_LBRR_idx].usage = sEncCtrl.sCmn.LBRR_usage;
                psEnc.sCmn.oldest_LBRR_idx = (psEnc.sCmn.oldest_LBRR_idx + 1) & 1;
                psEnc.sCmn.nFramesInPayloadBuf = 0;
            } else {
                pnBytesOut[0] = (short) 0;
                nBytes[0] = 0;
                psEnc.sCmn.nFramesInPayloadBuf = 0;
                ret = -4;
            }
        } else {
            pnBytesOut[0] = (short) 0;
            RangeCoder.SKP_Silk_range_encoder(psEnc.sCmn.sRC, 1, FrameTermination_CDF, 0);
            RangeCoder.SKP_Silk_range_coder_get_length(psEnc.sCmn.sRC, nBytes);
        }
        if (psEnc.sCmn.sRC.error != 0) {
            ret = -9;
        }
        psEnc.BufferedInChannel_ms += (8000.0f * ((float) (nBytes[0] - psEnc.sCmn.nBytesInPayloadBuf))) / ((float) psEnc.sCmn.TargetRate_bps);
        psEnc.BufferedInChannel_ms -= 20.0f;
        psEnc.BufferedInChannel_ms = SigProcFLP.SKP_LIMIT_float(psEnc.BufferedInChannel_ms, 0.0f, 100.0f);
        psEnc.sCmn.nBytesInPayloadBuf = nBytes[0];
        if (psEnc.speech_activity > 0.7f) {
            psEnc.sCmn.sSWBdetect.ActiveSpeech_ms = SigProcFIX.SKP_ADD_POS_SAT32(psEnc.sCmn.sSWBdetect.ActiveSpeech_ms, 20);
        }
        return ret;
    }

    static void SKP_Silk_LBRR_encode_FLP(SKP_Silk_encoder_state_FLP psEnc, SKP_Silk_encoder_control_FLP psEncCtrl, byte[] pCode, short[] pnBytesOut, float[] xfw) {
        int[] Gains_Q16 = new int[4];
        int[] TempGainsIndices = new int[4];
        int[] nBytes = new int[1];
        float[] TempGains = new float[4];
        int Rate_only_parameters = 0;
        ControlCodecFLP.SKP_Silk_LBRR_ctrl_FLP(psEnc, psEncCtrl.sCmn);
        if (psEnc.sCmn.LBRR_enabled != 0) {
            int nFramesInPayloadBuf;
            System.arraycopy(psEncCtrl.sCmn.GainsIndices, 0, TempGainsIndices, 0, 4);
            System.arraycopy(psEncCtrl.Gains, 0, TempGains, 0, 4);
            int typeOffset = psEnc.sCmn.typeOffsetPrev;
            int LTP_scaleIndex = psEncCtrl.sCmn.LTP_scaleIndex;
            if (psEnc.sCmn.fs_kHz == 8) {
                Rate_only_parameters = 13500;
            } else if (psEnc.sCmn.fs_kHz == 12) {
                Rate_only_parameters = 15500;
            } else if (psEnc.sCmn.fs_kHz == 16) {
                Rate_only_parameters = 17500;
            } else if (psEnc.sCmn.fs_kHz == 24) {
                Rate_only_parameters = 19500;
            } else if (!$assertionsDisabled) {
                throw new AssertionError();
            }
            if (psEnc.sCmn.Complexity <= 0 || psEnc.sCmn.TargetRate_bps <= Rate_only_parameters) {
                Arrays.fill(psEnc.sCmn.q_LBRR, (byte) 0);
                psEncCtrl.sCmn.LTP_scaleIndex = 0;
            } else {
                if (psEnc.sCmn.nFramesInPayloadBuf == 0) {
                    psEnc.sNSQ_LBRR = (SKP_Silk_nsq_state) psEnc.sNSQ.clone();
                    psEnc.sCmn.LBRRprevLastGainIndex = psEnc.sShape.LastGainIndex;
                    int[] iArr = psEncCtrl.sCmn.GainsIndices;
                    iArr[0] = iArr[0] + psEnc.sCmn.LBRR_GainIncreases;
                    psEncCtrl.sCmn.GainsIndices[0] = SigProcFIX.SKP_LIMIT(psEncCtrl.sCmn.GainsIndices[0], 0, 63);
                }
                int[] LBRRprevLastGainIndex_ptr = new int[]{psEnc.sCmn.LBRRprevLastGainIndex};
                GainQuant.SKP_Silk_gains_dequant(Gains_Q16, psEncCtrl.sCmn.GainsIndices, LBRRprevLastGainIndex_ptr, psEnc.sCmn.nFramesInPayloadBuf);
                psEnc.sCmn.LBRRprevLastGainIndex = LBRRprevLastGainIndex_ptr[0];
                for (int k = 0; k < 4; k++) {
                    psEncCtrl.Gains[k] = ((float) Gains_Q16[k]) / 65536.0f;
                }
                WrappersFLP.SKP_Silk_NSQ_wrapper_FLP(psEnc, psEncCtrl, xfw, 0, psEnc.sCmn.q_LBRR, 0, 1);
            }
            if (psEnc.sCmn.nFramesInPayloadBuf == 0) {
                RangeCoder.SKP_Silk_range_enc_init(psEnc.sCmn.sRC_LBRR);
                psEnc.sCmn.nBytesInPayloadBuf = 0;
            }
            EncodeParameters.SKP_Silk_encode_parameters(psEnc.sCmn, psEncCtrl.sCmn, psEnc.sCmn.sRC_LBRR, psEnc.sCmn.q_LBRR);
            if (psEnc.sCmn.sRC_LBRR.error != 0) {
                nFramesInPayloadBuf = 0;
            } else {
                nFramesInPayloadBuf = psEnc.sCmn.nFramesInPayloadBuf + 1;
            }
            if (Macros.SKP_SMULBB(nFramesInPayloadBuf, 20) >= psEnc.sCmn.PacketSize_ms) {
                RangeCoder.SKP_Silk_range_encoder(psEnc.sCmn.sRC_LBRR, 0, TablesOther.SKP_Silk_FrameTermination_CDF, 0);
                RangeCoder.SKP_Silk_range_coder_get_length(psEnc.sCmn.sRC_LBRR, nBytes);
                if (pnBytesOut[0] >= nBytes[0]) {
                    RangeCoder.SKP_Silk_range_enc_wrap_up(psEnc.sCmn.sRC_LBRR);
                    System.arraycopy(psEnc.sCmn.sRC_LBRR.buffer, 0, pCode, 0, nBytes[0]);
                    pnBytesOut[0] = (short) nBytes[0];
                } else {
                    pnBytesOut[0] = (short) 0;
                    if (!$assertionsDisabled) {
                        throw new AssertionError();
                    }
                }
            }
            pnBytesOut[0] = (short) 0;
            RangeCoder.SKP_Silk_range_encoder(psEnc.sCmn.sRC_LBRR, 1, TablesOther.SKP_Silk_FrameTermination_CDF, 0);
            System.arraycopy(TempGainsIndices, 0, psEncCtrl.sCmn.GainsIndices, 0, 4);
            System.arraycopy(TempGains, 0, psEncCtrl.Gains, 0, 4);
            psEncCtrl.sCmn.LTP_scaleIndex = LTP_scaleIndex;
            psEnc.sCmn.typeOffsetPrev = typeOffset;
        }
    }
}
