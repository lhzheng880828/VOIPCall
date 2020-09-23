package org.jitsi.impl.neomedia.codec.audio.silk;

import javax.media.Buffer;

public class InitEncoderFLP {
    static int SKP_Silk_init_encoder_FLP(SKP_Silk_encoder_state_FLP psEnc) {
        int ret = ControlCodecFLP.SKP_Silk_control_encoder_FLP(psEnc, 24000, 24, 20, 25, 0, 0, 0, 10, 0);
        psEnc.variable_HP_smth1 = MainFLP.SKP_Silk_log2(70.0d);
        psEnc.variable_HP_smth2 = MainFLP.SKP_Silk_log2(70.0d);
        psEnc.sCmn.first_frame_after_reset = 1;
        psEnc.sCmn.fs_kHz_changed = 0;
        psEnc.sCmn.LBRR_enabled = 0;
        ret += VAD.SKP_Silk_VAD_Init(psEnc.sCmn.sVAD);
        psEnc.sNSQ.prev_inv_gain_Q16 = Buffer.FLAG_SKIP_FEC;
        psEnc.sNSQ_LBRR.prev_inv_gain_Q16 = Buffer.FLAG_SKIP_FEC;
        return ret;
    }
}
