package org.jitsi.impl.neomedia.codec.audio.silk;

public class LowpassShort {
    static void SKP_Silk_lowpass_short(short[] in, int in_offset, int[] S, int S_offset, int[] out, int out_offset, int len) {
        int state = S[S_offset + 0];
        for (int k = 0; k < len; k++) {
            int in_tmp = in[in_offset + k] * 768;
            int out_tmp = state + in_tmp;
            state = in_tmp - (out_tmp >> 1);
            out[out_offset + k] = out_tmp;
        }
        S[S_offset + 0] = state;
    }
}
