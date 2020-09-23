package org.jitsi.impl.neomedia.codec.audio.silk;

public class ResamplerPrivateDown4 {
    static void SKP_Silk_resampler_private_down4(int[] S, int S_offset, short[] out, int out_offset, short[] in, int in_offset, int inLen) {
        int len4 = inLen >> 2;
        for (int k = 0; k < len4; k++) {
            int in32 = (in[(k * 4) + in_offset] + in[((k * 4) + in_offset) + 1]) << 9;
            int Y = in32 - S[S_offset];
            int X = Macros.SKP_SMLAWB(Y, Y, -25727);
            int out32 = S[S_offset] + X;
            S[S_offset] = in32 + X;
            in32 = (in[((k * 4) + in_offset) + 2] + in[((k * 4) + in_offset) + 3]) << 9;
            X = Macros.SKP_SMULWB(in32 - S[S_offset + 1], 9872);
            out32 = (out32 + S[S_offset + 1]) + X;
            S[S_offset + 1] = in32 + X;
            out[out_offset + k] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(out32, 11));
        }
    }
}
