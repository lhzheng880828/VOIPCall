package org.jitsi.impl.neomedia.codec.audio.silk;

public class ResamplerUp2 {
    static void SKP_Silk_resampler_up2(int[] S, int S_offset, short[] out, int out_offset, short[] in, int in_offset, int len) {
        for (int k = 0; k < len; k++) {
            int in32 = in[in_offset + k] << 10;
            int X = Macros.SKP_SMULWB(in32 - S[S_offset + 0], 8102);
            int out32 = S[S_offset + 0] + X;
            S[S_offset + 0] = in32 + X;
            out[(k * 2) + out_offset] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(out32, 10));
            int Y = in32 - S[S_offset + 1];
            X = Macros.SKP_SMLAWB(Y, Y, -28753);
            out32 = S[S_offset + 1] + X;
            S[S_offset + 1] = in32 + X;
            out[((k * 2) + out_offset) + 1] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(out32, 10));
        }
    }
}