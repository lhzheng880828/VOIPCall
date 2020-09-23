package org.jitsi.impl.neomedia.codec.audio.silk;

import javax.media.Buffer;

public class CreateInitDestroy {
    static int SKP_Silk_init_decoder(SKP_Silk_decoder_state psDec) {
        DecoderSetFs.SKP_Silk_decoder_set_fs(psDec, 24);
        psDec.first_frame_after_reset = 1;
        psDec.prev_inv_gain_Q16 = Buffer.FLAG_SKIP_FEC;
        CNG.SKP_Silk_CNG_Reset(psDec);
        PLC.SKP_Silk_PLC_Reset(psDec);
        return 0;
    }
}
