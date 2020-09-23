package org.jitsi.impl.neomedia.codec.audio.silk;

public class LBRRReset {
    static void SKP_Silk_LBRR_reset(SKP_Silk_encoder_state psEncC) {
        for (int i = 0; i < 2; i++) {
            psEncC.LBRR_buffer[i].usage = 0;
        }
    }
}
