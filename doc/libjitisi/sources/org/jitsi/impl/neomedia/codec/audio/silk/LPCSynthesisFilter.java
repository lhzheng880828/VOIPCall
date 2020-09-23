package org.jitsi.impl.neomedia.codec.audio.silk;

public class LPCSynthesisFilter {
    static void SKP_Silk_LPC_synthesis_filter(short[] in, short[] A_Q12, int Gain_Q26, int[] S, short[] out, int len, int Order) {
        int Order_half = Order >> 1;
        Typedef.SKP_assert(Order_half * 2 == Order);
        for (int k = 0; k < len; k++) {
            int SB;
            int SA = S[Order - 1];
            int out32_Q10 = 0;
            for (int j = 0; j < Order_half - 1; j++) {
                int idx = Macros.SKP_SMULBB(2, j) + 1;
                SB = S[(Order - 1) - idx];
                S[(Order - 1) - idx] = SA;
                out32_Q10 = Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(out32_Q10, SA, A_Q12[j << 1]), SB, A_Q12[(j << 1) + 1]);
                SA = S[(Order - 2) - idx];
                S[(Order - 2) - idx] = SB;
            }
            SB = S[0];
            S[0] = SA;
            out32_Q10 = Macros.SKP_ADD_SAT32(Macros.SKP_SMLAWB(Macros.SKP_SMLAWB(out32_Q10, SA, A_Q12[Order - 2]), SB, A_Q12[Order - 1]), Macros.SKP_SMULWB(Gain_Q26, in[k]));
            out[k] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(out32_Q10, 10));
            S[Order - 1] = SigProcFIX.SKP_LSHIFT_SAT32(out32_Q10, 4);
        }
    }
}
