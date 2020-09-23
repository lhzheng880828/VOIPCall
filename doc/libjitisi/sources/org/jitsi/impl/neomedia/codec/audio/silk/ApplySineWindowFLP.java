package org.jitsi.impl.neomedia.codec.audio.silk;

public class ApplySineWindowFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!ApplySineWindowFLP.class.desiredAssertionStatus());

    static void SKP_Silk_apply_sine_window_FLP(float[] px_win, int px_win_offset, float[] px, int px_offset, int win_type, int length) {
        if ($assertionsDisabled || (length & 3) == 0) {
            float S0;
            float S1;
            float freq = 3.1415927f / ((float) (length + 1));
            if (win_type == 0) {
                freq *= 2.0f;
            }
            float c = 2.0f - (freq * freq);
            if (win_type < 2) {
                S0 = 0.0f;
                S1 = freq;
            } else {
                S0 = 1.0f;
                S1 = 0.5f * c;
            }
            for (int k = 0; k < length; k += 4) {
                px_win[(px_win_offset + k) + 0] = (px[(px_offset + k) + 0] * 0.5f) * (S0 + S1);
                px_win[(px_win_offset + k) + 1] = px[(px_offset + k) + 1] * S1;
                S0 = (c * S1) - S0;
                px_win[(px_win_offset + k) + 2] = (px[(px_offset + k) + 2] * 0.5f) * (S1 + S0);
                px_win[(px_win_offset + k) + 3] = px[(px_offset + k) + 3] * S0;
                S1 = (c * S0) - S1;
            }
            return;
        }
        throw new AssertionError();
    }
}
