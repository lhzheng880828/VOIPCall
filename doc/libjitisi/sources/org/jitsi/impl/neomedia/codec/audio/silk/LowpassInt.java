package org.jitsi.impl.neomedia.codec.audio.silk;

public class LowpassInt {
    static void SKP_Silk_lowpass_int(int[] in, int in_offset, int[] S, int S_offset, int[] out, int out_offset, int len) {
        int state = S[S_offset + 0];
        int k = len;
        int out_offset2 = out_offset;
        int in_offset2 = in_offset;
        while (k > 0) {
            in_offset = in_offset2 + 1;
            int in_tmp = in[in_offset2];
            in_tmp -= in_tmp >> 2;
            int out_tmp = state + in_tmp;
            state = in_tmp - (out_tmp >> 1);
            out_offset = out_offset2 + 1;
            out[out_offset2] = out_tmp;
            k--;
            out_offset2 = out_offset;
            in_offset2 = in_offset;
        }
        S[S_offset + 0] = state;
    }
}
