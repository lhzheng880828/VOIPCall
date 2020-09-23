package org.jitsi.impl.neomedia.codec.audio.silk;

public class AllpassInt {
    static void SKP_Silk_allpass_int(int[] in, int in_offset, int[] S, int S_offset, int A, int[] out, int out_offset, int len) {
        int S0 = S[S_offset + 0];
        int k = len - 1;
        int out_offset2 = out_offset;
        int in_offset2 = in_offset;
        while (k >= 0) {
            int Y2 = in[in_offset2 + 0] - S0;
            int X2 = ((Y2 >> 15) * A) + (((Y2 & 32767) * A) >> 15);
            out_offset = out_offset2 + 1;
            out[out_offset2] = S0 + X2;
            S0 = in[in_offset2] + X2;
            k--;
            out_offset2 = out_offset;
            in_offset2++;
        }
        S[S_offset + 0] = S0;
    }
}
