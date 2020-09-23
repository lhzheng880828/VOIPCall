package org.jitsi.impl.neomedia.codec.audio.silk;

public class ResamplerPrivateARMA4 {
    static void SKP_Silk_resampler_private_ARMA4(int[] S, int S_offset, short[] out, int out_offset, short[] in, int in_offset, short[] Coef, int Coef_offset, int len) {
        for (int k = 0; k < len; k++) {
            int in_Q8 = in[in_offset + k] << 8;
            int out1_Q8 = in_Q8 + (S[S_offset] << 2);
            int out2_Q8 = out1_Q8 + (S[S_offset + 2] << 2);
            S[S_offset] = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(S[S_offset + 1], in_Q8, Coef[Coef_offset]), out1_Q8, Coef[Coef_offset + 2]);
            S[S_offset + 2] = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(S[S_offset + 3], out1_Q8, Coef[Coef_offset + 1]), out2_Q8, Coef[Coef_offset + 4]);
            S[S_offset + 1] = Macros.SKP_SMLAWB(in_Q8 >> 2, out1_Q8, Coef[Coef_offset + 3]);
            S[S_offset + 3] = Macros.SKP_SMLAWB(out1_Q8 >> 2, out2_Q8, Coef[Coef_offset + 5]);
            out[out_offset + k] = (short) SigProcFIX.SKP_SAT16(Macros.SKP_SMLAWB(128, out2_Q8, Coef[Coef_offset + 6]) >> 8);
        }
    }
}
