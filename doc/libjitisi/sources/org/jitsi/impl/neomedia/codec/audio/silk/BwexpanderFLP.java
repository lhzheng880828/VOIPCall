package org.jitsi.impl.neomedia.codec.audio.silk;

public class BwexpanderFLP {
    static void SKP_Silk_bwexpander_FLP(float[] ar, int ar_offset, int d, float chirp) {
        int i;
        float cfac = chirp;
        for (int i2 = 0; i2 < d - 1; i2++) {
            i = ar_offset + i2;
            ar[i] = ar[i] * cfac;
            cfac *= chirp;
        }
        i = (ar_offset + d) - 1;
        ar[i] = ar[i] * cfac;
    }
}
