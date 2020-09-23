package org.jitsi.impl.neomedia.codec.audio.silk;

public class BiquadAlt {
    static void SKP_Silk_biquad_alt(short[] in, int in_offset, int[] B_Q28, int[] A_Q28, int[] S, short[] out, int out_offset, int len) {
        int A0_L_Q28 = (-A_Q28[0]) & 16383;
        int A0_U_Q28 = (-A_Q28[0]) >> 14;
        int A1_L_Q28 = (-A_Q28[1]) & 16383;
        int A1_U_Q28 = (-A_Q28[1]) >> 14;
        for (int k = 0; k < len; k++) {
            int inval = in[in_offset + k];
            int out32_Q14 = Macros.SKP_SMLAWB(S[0], B_Q28[0], inval) << 2;
            S[0] = S[1] + (Macros.SKP_SMULWB(out32_Q14, A0_L_Q28) >> 14);
            S[0] = Macros.SKP_SMLAWB(S[0], out32_Q14, A0_U_Q28);
            S[0] = Macros.SKP_SMLAWB(S[0], B_Q28[1], inval);
            S[1] = Macros.SKP_SMULWB(out32_Q14, A1_L_Q28) >> 14;
            S[1] = Macros.SKP_SMLAWB(S[1], out32_Q14, A1_U_Q28);
            S[1] = Macros.SKP_SMLAWB(S[1], B_Q28[2], inval);
            out[out_offset + k] = (short) SigProcFIX.SKP_SAT16((out32_Q14 >> 14) + 2);
        }
    }
}
