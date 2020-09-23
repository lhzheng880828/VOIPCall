package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class DecodeFrame {
    static int SKP_Silk_decode_frame(SKP_Silk_decoder_state psDec, short[] pOut, int pOut_offset, short[] pN, byte[] pCode, int pCode_offset, int nBytes, int action, int[] decBytes) {
        SKP_Silk_decoder_control sDecCtrl = new SKP_Silk_decoder_control();
        int ret = 0;
        int[] Pulses = new int[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        int L = psDec.frame_length;
        sDecCtrl.LTP_scale_Q14 = 0;
        boolean z = L > 0 && L <= DeviceConfiguration.DEFAULT_VIDEO_HEIGHT;
        Typedef.SKP_assert(z);
        decBytes[0] = 0;
        if (action == 0) {
            int fs_Khz_old = psDec.fs_kHz;
            int LPC_order_old = psDec.LPC_order;
            if (psDec.nFramesDecoded == 0) {
                RangeCoder.SKP_Silk_range_dec_init(psDec.sRC, pCode, pCode_offset, nBytes);
            }
            DecodeParameters.SKP_Silk_decode_parameters(psDec, sDecCtrl, Pulses, 1);
            if (psDec.sRC.error != 0) {
                psDec.nBytesLeft = 0;
                action = 1;
                DecoderSetFs.SKP_Silk_decoder_set_fs(psDec, fs_Khz_old);
                decBytes[0] = psDec.sRC.bufferLength;
                ret = psDec.sRC.error == -8 ? -11 : -12;
            } else {
                decBytes[0] = psDec.sRC.bufferLength - psDec.nBytesLeft;
                psDec.nFramesDecoded++;
                L = psDec.frame_length;
                DecodeCore.SKP_Silk_decode_core(psDec, sDecCtrl, pOut, pOut_offset, Pulses);
                PLC.SKP_Silk_PLC(psDec, sDecCtrl, pOut, pOut_offset, L, action);
                psDec.lossCnt = 0;
                psDec.prev_sigtype = sDecCtrl.sigtype;
                psDec.first_frame_after_reset = 0;
            }
        }
        if (action == 1) {
            PLC.SKP_Silk_PLC(psDec, sDecCtrl, pOut, pOut_offset, L, action);
            psDec.lossCnt++;
        }
        System.arraycopy(pOut, pOut_offset + 0, psDec.outBuf, 0, L);
        PLC.SKP_Silk_PLC_glue_frames(psDec, sDecCtrl, pOut, pOut_offset, L);
        CNG.SKP_Silk_CNG(psDec, sDecCtrl, pOut, pOut_offset, L);
        z = (psDec.fs_kHz == 12 && L % 3 == 0) || (psDec.fs_kHz != 12 && L % 2 == 0);
        Typedef.SKP_assert(z);
        Biquad.SKP_Silk_biquad(pOut, pOut_offset, psDec.HP_B, psDec.HP_A, psDec.HPState, pOut, pOut_offset, L);
        pN[0] = (short) L;
        psDec.lagPrev = sDecCtrl.pitchL[3];
        return ret;
    }
}
