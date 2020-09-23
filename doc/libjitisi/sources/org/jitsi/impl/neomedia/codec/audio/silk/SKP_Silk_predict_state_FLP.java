package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;

/* compiled from: StructsFLP */
class SKP_Silk_predict_state_FLP {
    int max_pitch_lag;
    int min_pitch_lag;
    int pitch_LPC_win_length;
    float[] prev_NLSFq = new float[16];

    SKP_Silk_predict_state_FLP() {
    }

    public void memZero() {
        this.pitch_LPC_win_length = 0;
        this.max_pitch_lag = 0;
        this.min_pitch_lag = 0;
        Arrays.fill(this.prev_NLSFq, 0.0f);
    }
}
