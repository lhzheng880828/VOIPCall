package org.jitsi.impl.neomedia.codec.audio.silk;

import javax.media.Buffer;

public class Bwexpander {
    static void SKP_Silk_bwexpander(short[] ar, int d, int chirp_Q16) {
        int chirp_minus_one_Q16 = chirp_Q16 - Buffer.FLAG_SKIP_FEC;
        for (int i = 0; i < d - 1; i++) {
            ar[i] = (short) SigProcFIX.SKP_RSHIFT_ROUND(ar[i] * chirp_Q16, 16);
            chirp_Q16 += SigProcFIX.SKP_RSHIFT_ROUND(chirp_Q16 * chirp_minus_one_Q16, 16);
        }
        ar[d - 1] = (short) SigProcFIX.SKP_RSHIFT_ROUND(ar[d - 1] * chirp_Q16, 16);
    }
}
