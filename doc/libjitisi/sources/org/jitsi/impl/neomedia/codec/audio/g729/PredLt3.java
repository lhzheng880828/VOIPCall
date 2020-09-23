package org.jitsi.impl.neomedia.codec.audio.g729;

class PredLt3 {
    PredLt3() {
    }

    static void pred_lt_3(float[] exc, int exc_offset, int t0, int frac, int l_subfr) {
        float[] inter_3l = TabLd8k.inter_3l;
        int x0 = exc_offset - t0;
        frac = -frac;
        if (frac < 0) {
            frac += 3;
            x0--;
        }
        for (int j = 0; j < l_subfr; j++) {
            int x1 = x0;
            x0++;
            int x2 = x0;
            int c1 = frac;
            int c2 = 3 - frac;
            float s = 0.0f;
            int i = 0;
            int k = 0;
            while (i < 10) {
                s += (exc[x1 - i] * inter_3l[c1 + k]) + (exc[x2 + i] * inter_3l[c2 + k]);
                i++;
                k += 3;
            }
            exc[exc_offset + j] = s;
        }
    }
}
