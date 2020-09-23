package org.jitsi.impl.neomedia.codec.audio.silk;

public class AllpassIntFLP {
    static void SKP_Silk_allpass_int_FLP(float[] in, int in_offset, float[] S, int S_offset, float A, float[] out, int out_offset, int len) {
        float S0 = S[S_offset];
        int k = len - 1;
        int out_offset2 = out_offset;
        int in_offset2 = in_offset;
        while (k >= 0) {
            float X2 = (in[in_offset2] - S0) * A;
            out_offset = out_offset2 + 1;
            out[out_offset2] = S0 + X2;
            S0 = in[in_offset2] + X2;
            k--;
            out_offset2 = out_offset;
            in_offset2++;
        }
        S[S_offset] = S0;
    }
}
