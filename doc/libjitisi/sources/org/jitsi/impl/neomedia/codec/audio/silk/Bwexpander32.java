package org.jitsi.impl.neomedia.codec.audio.silk;

public class Bwexpander32 {
    static void SKP_Silk_bwexpander_32(int[] ar, int d, int chirp_Q16) {
        int tmp_chirp_Q16 = chirp_Q16;
        for (int i = 0; i < d - 1; i++) {
            ar[i] = Macros.SKP_SMULWW(ar[i], tmp_chirp_Q16);
            tmp_chirp_Q16 = Macros.SKP_SMULWW(chirp_Q16, tmp_chirp_Q16);
        }
        ar[d - 1] = Macros.SKP_SMULWW(ar[d - 1], tmp_chirp_Q16);
    }
}
