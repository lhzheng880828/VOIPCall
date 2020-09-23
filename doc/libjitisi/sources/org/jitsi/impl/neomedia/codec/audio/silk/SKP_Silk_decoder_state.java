package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.device.DeviceConfiguration;

/* compiled from: Structs */
class SKP_Silk_decoder_state {
    int FrameTermination;
    int[] HPState = new int[2];
    short[] HP_A;
    short[] HP_B;
    int LPC_order;
    int LastGainIndex;
    int LastGainIndex_EnhLayer;
    int[] exc_Q10 = new int[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
    int first_frame_after_reset;
    int frame_length;
    int fs_kHz;
    int inband_FEC_offset;
    int lagPrev;
    int lossCnt;
    int moreInternalDecoderFrames;
    int nBytesLeft;
    int nFramesDecoded;
    int nFramesInPacket;
    int no_FEC_counter;
    short[] outBuf = new short[960];
    int[] prevNLSF_Q15 = new int[16];
    int prev_API_sampleRate;
    int prev_inv_gain_Q16;
    int prev_sigtype;
    SKP_Silk_NLSF_CB_struct[] psNLSF_CB = new SKP_Silk_NLSF_CB_struct[2];
    int[] res_Q10 = new int[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
    SKP_Silk_resampler_state_struct resampler_state = new SKP_Silk_resampler_state_struct();
    SKP_Silk_CNG_struct sCNG = new SKP_Silk_CNG_struct();
    int[] sLPC_Q14 = new int[136];
    int[] sLTP_Q16 = new int[960];
    int sLTP_buf_idx;
    SKP_Silk_PLC_struct sPLC = new SKP_Silk_PLC_struct();
    SKP_Silk_range_coder_state sRC = new SKP_Silk_range_coder_state();
    int subfr_length;
    int typeOffsetPrev;
    int vadFlag;

    SKP_Silk_decoder_state() {
    }
}
