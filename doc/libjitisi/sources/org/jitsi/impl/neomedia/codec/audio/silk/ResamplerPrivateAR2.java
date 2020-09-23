package org.jitsi.impl.neomedia.codec.audio.silk;

public class ResamplerPrivateAR2 {
    static void SKP_Silk_resampler_private_AR2(int[] S, int S_offset, int[] out_Q8, int out_Q8_offset, short[] in, int in_offset, short[] A_Q14, int A_Q14_offset, int len) {
        for (int k = 0; k < len; k++) {
            int out32 = S[S_offset] + (in[in_offset + k] << 8);
            out_Q8[out_Q8_offset + k] = out32;
            out32 <<= 2;
            S[S_offset] = Macros.SKP_SMLAWB(S[S_offset + 1], out32, A_Q14[A_Q14_offset]);
            S[S_offset + 1] = Macros.SKP_SMULWB(out32, A_Q14[A_Q14_offset + 1]);
        }
    }
}
