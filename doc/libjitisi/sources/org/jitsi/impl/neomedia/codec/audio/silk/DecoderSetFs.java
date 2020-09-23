package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;
import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class DecoderSetFs {
    static void SKP_Silk_decoder_set_fs(SKP_Silk_decoder_state psDec, int fs_kHz) {
        boolean z = true;
        if (psDec.fs_kHz != fs_kHz) {
            psDec.fs_kHz = fs_kHz;
            psDec.frame_length = fs_kHz * 20;
            psDec.subfr_length = fs_kHz * 5;
            if (psDec.fs_kHz == 8) {
                psDec.LPC_order = 10;
                psDec.psNLSF_CB[0] = TablesNLSFCB010.SKP_Silk_NLSF_CB0_10;
                psDec.psNLSF_CB[1] = TablesNLSFCB110.SKP_Silk_NLSF_CB1_10;
            } else {
                psDec.LPC_order = 16;
                psDec.psNLSF_CB[0] = TablesNLSFCB016.SKP_Silk_NLSF_CB0_16;
                psDec.psNLSF_CB[1] = TablesNLSFCB116.SKP_Silk_NLSF_CB1_16;
            }
            Arrays.fill(psDec.sLPC_Q14, 0, 16, 0);
            Arrays.fill(psDec.outBuf, 0, DeviceConfiguration.DEFAULT_VIDEO_HEIGHT, (short) 0);
            Arrays.fill(psDec.prevNLSF_Q15, 0, 16, 0);
            psDec.sLTP_buf_idx = 0;
            psDec.lagPrev = 100;
            psDec.LastGainIndex = 1;
            psDec.prev_sigtype = 0;
            psDec.first_frame_after_reset = 1;
            if (fs_kHz == 24) {
                psDec.HP_A = TablesOther.SKP_Silk_Dec_A_HP_24;
                psDec.HP_B = TablesOther.SKP_Silk_Dec_B_HP_24;
            } else if (fs_kHz == 16) {
                psDec.HP_A = TablesOther.SKP_Silk_Dec_A_HP_16;
                psDec.HP_B = TablesOther.SKP_Silk_Dec_B_HP_16;
            } else if (fs_kHz == 12) {
                psDec.HP_A = TablesOther.SKP_Silk_Dec_A_HP_12;
                psDec.HP_B = TablesOther.SKP_Silk_Dec_B_HP_12;
            } else if (fs_kHz == 8) {
                psDec.HP_A = TablesOther.SKP_Silk_Dec_A_HP_8;
                psDec.HP_B = TablesOther.SKP_Silk_Dec_B_HP_8;
            } else {
                Typedef.SKP_assert(false);
            }
        }
        if (psDec.frame_length <= 0 || psDec.frame_length > DeviceConfiguration.DEFAULT_VIDEO_HEIGHT) {
            z = false;
        }
        Typedef.SKP_assert(z);
    }
}
