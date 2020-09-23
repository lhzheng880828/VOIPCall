package org.jitsi.impl.neomedia.codec.audio.silk;

public class ResamplerDown2 {
    static void SKP_Silk_resampler_down2(int[] S, int S_offset, short[] out, int out_offset, short[] in, int in_offset, int inLen) {
        int len2 = inLen >> 1;
        for (int k = 0; k < len2; k++) {
            int in32 = in[(k * 2) + in_offset] << 10;
            int Y = in32 - S[S_offset];
            int X = Macros.SKP_SMLAWB(Y, Y, -25727);
            int out32 = S[S_offset] + X;
            S[S_offset] = in32 + X;
            in32 = in[((k * 2) + in_offset) + 1] << 10;
            X = Macros.SKP_SMULWB(in32 - S[S_offset + 1], 9872);
            out32 = (out32 + S[S_offset + 1]) + X;
            S[S_offset + 1] = in32 + X;
            out[out_offset + k] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(out32, 11));
        }
    }
}
