package org.jitsi.impl.neomedia.codec.audio.silk;

public class LPCSynthesisOrder16 {
    static void SKP_Silk_LPC_synthesis_order16(short[] in, short[] A_Q12, int Gain_Q26, int[] S, short[] out, int len) {
        for (int k = 0; k < len; k++) {
            int SA = S[15];
            int SB = S[14];
            S[14] = SA;
            int out32_Q10 = SigProcFIX.SKP_SMLAWB_ovflw(Macros.SKP_SMULWB(SA, A_Q12[0]), SB, A_Q12[1]);
            SA = S[13];
            S[13] = SB;
            SB = S[12];
            S[12] = SA;
            out32_Q10 = SigProcFIX.SKP_SMLAWB_ovflw(SigProcFIX.SKP_SMLAWB_ovflw(out32_Q10, SA, A_Q12[2]), SB, A_Q12[3]);
            SA = S[11];
            S[11] = SB;
            SB = S[10];
            S[10] = SA;
            out32_Q10 = SigProcFIX.SKP_SMLAWB_ovflw(SigProcFIX.SKP_SMLAWB_ovflw(out32_Q10, SA, A_Q12[4]), SB, A_Q12[5]);
            SA = S[9];
            S[9] = SB;
            SB = S[8];
            S[8] = SA;
            out32_Q10 = SigProcFIX.SKP_SMLAWB_ovflw(SigProcFIX.SKP_SMLAWB_ovflw(out32_Q10, SA, A_Q12[6]), SB, A_Q12[7]);
            SA = S[7];
            S[7] = SB;
            SB = S[6];
            S[6] = SA;
            out32_Q10 = SigProcFIX.SKP_SMLAWB_ovflw(SigProcFIX.SKP_SMLAWB_ovflw(out32_Q10, SA, A_Q12[8]), SB, A_Q12[9]);
            SA = S[5];
            S[5] = SB;
            SB = S[4];
            S[4] = SA;
            out32_Q10 = SigProcFIX.SKP_SMLAWB_ovflw(SigProcFIX.SKP_SMLAWB_ovflw(out32_Q10, SA, A_Q12[10]), SB, A_Q12[11]);
            SA = S[3];
            S[3] = SB;
            SB = S[2];
            S[2] = SA;
            out32_Q10 = SigProcFIX.SKP_SMLAWB_ovflw(SigProcFIX.SKP_SMLAWB_ovflw(out32_Q10, SA, A_Q12[12]), SB, A_Q12[13]);
            SA = S[1];
            S[1] = SB;
            SB = S[0];
            S[0] = SA;
            out32_Q10 = Macros.SKP_ADD_SAT32(SigProcFIX.SKP_SMLAWB_ovflw(SigProcFIX.SKP_SMLAWB_ovflw(out32_Q10, SA, A_Q12[14]), SB, A_Q12[15]), Macros.SKP_SMULWB(Gain_Q26, in[k]));
            out[k] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(out32_Q10, 10));
            S[15] = SigProcFIX.SKP_LSHIFT_SAT32(out32_Q10, 4);
        }
    }
}
