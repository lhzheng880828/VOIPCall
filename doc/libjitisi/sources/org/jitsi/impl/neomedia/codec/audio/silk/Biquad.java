package org.jitsi.impl.neomedia.codec.audio.silk;

public class Biquad {
    static void SKP_Silk_biquad(short[] in, int in_offset, short[] B, short[] A, int[] S, short[] out, int out_offset, int len) {
        int S0 = S[0];
        int S1 = S[1];
        int A0_neg = -A[0];
        int A1_neg = -A[1];
        for (int k = 0; k < len; k++) {
            int in16 = in[in_offset + k];
            int out32 = Macros.SKP_SMLABB(S0, in16, B[0]);
            S0 = Macros.SKP_SMLABB(S1, in16, B[1]) + (Macros.SKP_SMULWB(out32, A0_neg) << 3);
            S1 = Macros.SKP_SMLABB(Macros.SKP_SMULWB(out32, A1_neg) << 3, in16, B[2]);
            out[out_offset + k] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(out32, 13) + 1);
        }
        S[0] = S0;
        S[1] = S1;
    }
}
