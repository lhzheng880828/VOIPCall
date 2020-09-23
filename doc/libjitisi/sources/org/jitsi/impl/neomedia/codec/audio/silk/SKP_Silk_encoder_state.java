package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.device.DeviceConfiguration;

/* compiled from: Structs */
class SKP_Silk_encoder_state {
    int API_fs_Hz;
    int Complexity;
    int[] In_HP_State = new int[2];
    int LBRR_GainIncreases;
    SKP_SILK_LBRR_struct[] LBRR_buffer = new SKP_SILK_LBRR_struct[2];
    int LBRR_enabled;
    int LBRRprevLastGainIndex;
    int LTPQuantLowComplexity;
    int NLSF_MSVQ_Survivors;
    int PacketLoss_perc;
    int PacketSize_ms;
    int TargetRate_bps;
    int bitrateDiff;
    int bitrate_threshold_down;
    int bitrate_threshold_up;
    int first_frame_after_reset;
    int frameCounter;
    int frame_length;
    int frames_since_onset;
    int fs_kHz;
    int fs_kHz_changed;
    int inDTX;
    short[] inputBuf = new short[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
    int inputBufIx;
    int la_pitch;
    int la_shape;
    int maxInternal_fs_kHz;
    int nBytesInPayloadBuf;
    int nFramesInPayloadBuf;
    int nStatesDelayedDecision;
    int noSpeechCounter;
    int oldest_LBRR_idx;
    int pitchEstimationComplexity;
    int pitchEstimationLPCOrder;
    int predictLPCOrder;
    int prevLag;
    int prev_API_fs_Hz;
    int prev_lagIndex;
    int prev_sigtype;
    SKP_Silk_NLSF_CB_struct[] psNLSF_CB = new SKP_Silk_NLSF_CB_struct[2];
    byte[] q;
    byte[] q_LBRR;
    SKP_Silk_resampler_state_struct resampler_state;
    SKP_Silk_LP_state sLP = new SKP_Silk_LP_state();
    SKP_Silk_range_coder_state sRC = new SKP_Silk_range_coder_state();
    SKP_Silk_range_coder_state sRC_LBRR = new SKP_Silk_range_coder_state();
    SKP_Silk_detect_SWB_state sSWBdetect;
    SKP_Silk_VAD_state sVAD = new SKP_Silk_VAD_state();
    int shapingLPCOrder;
    int subfr_length;
    int typeOffsetPrev;
    int useDTX;
    int useInBandFEC;
    int useInterpolatedNLSFs;
    int vadFlag;

    SKP_Silk_encoder_state() {
        for (int LBRR_bufferIni_i = 0; LBRR_bufferIni_i < 2; LBRR_bufferIni_i++) {
            this.LBRR_buffer[LBRR_bufferIni_i] = new SKP_SILK_LBRR_struct();
        }
        this.resampler_state = new SKP_Silk_resampler_state_struct();
        this.sSWBdetect = new SKP_Silk_detect_SWB_state();
        this.q = new byte[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        this.q_LBRR = new byte[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
    }
}
