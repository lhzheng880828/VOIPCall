package org.jitsi.impl.neomedia.codec.audio.silk;

public class MA {
    static void SKP_Silk_MA(short[] in, int in_offset, short[] B, int[] S, short[] out, int out_offset, int len, int order) {
        for (int k = 0; k < len; k++) {
            int in16 = in[in_offset + k];
            int out32 = SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SMLABB(S[0], in16, B[0]), 13);
            for (int d = 1; d < order; d++) {
                S[d - 1] = Macros.SKP_SMLABB(S[d], in16, B[d]);
            }
            S[order - 1] = Macros.SKP_SMULBB(in16, B[order]);
            out[out_offset + k] = (short) SigProcFIX.SKP_SAT16(out32);
        }
    }

    static void SKP_Silk_MA_Prediction(short[] in, int in_offset, short[] B, int B_offset, int[] S, short[] out, int out_offset, int len, int order) {
        for (int k = 0; k < len; k++) {
            int in16 = in[in_offset + k];
            int out32 = SigProcFIX.SKP_RSHIFT_ROUND((in16 << 12) - S[0], 12);
            for (int d = 0; d < order - 1; d++) {
                S[d] = SigProcFIX.SKP_SMLABB_ovflw(S[d + 1], in16, B[B_offset + d]);
            }
            S[order - 1] = Macros.SKP_SMULBB(in16, B[(B_offset + order) - 1]);
            out[out_offset + k] = (short) SigProcFIX.SKP_SAT16(out32);
        }
    }

    static void SKP_Silk_MA_Prediction_Q13(short[] in, int in_offset, short[] B, int[] S, short[] out, int out_offset, int len, int order) {
        for (int k = 0; k < len; k++) {
            int in16 = in[in_offset + k];
            int out32 = SigProcFIX.SKP_RSHIFT_ROUND((in16 << 13) - S[0], 13);
            for (int d = 0; d < order - 1; d++) {
                S[d] = Macros.SKP_SMLABB(S[d + 1], in16, B[d]);
            }
            S[order - 1] = Macros.SKP_SMULBB(in16, B[order - 1]);
            out[out_offset + k] = (short) SigProcFIX.SKP_SAT16(out32);
        }
    }

    static void SKP_Silk_LPC_analysis_filter(short[] in, int in_offset, short[] B, short[] S, short[] out, int out_offset, int len, int Order) {
        int Order_half = Order >> 1;
        Typedef.SKP_assert(Order_half * 2 == Order);
        for (int k = 0; k < len; k++) {
            short SB;
            short SA = S[0];
            int out32_Q12 = 0;
            for (int j = 0; j < Order_half - 1; j++) {
                int idx = Macros.SKP_SMULBB(2, j) + 1;
                SB = S[idx];
                S[idx] = SA;
                out32_Q12 = Macros.SKP_SMLABB(Macros.SKP_SMLABB(out32_Q12, SA, B[idx - 1]), SB, B[idx]);
                SA = S[idx + 1];
                S[idx + 1] = SB;
            }
            SB = S[Order - 1];
            S[Order - 1] = SA;
            out[out_offset + k] = (short) SigProcFIX.SKP_SAT16(SigProcFIX.SKP_RSHIFT_ROUND(Macros.SKP_SUB_SAT32(in[in_offset + k] << 12, Macros.SKP_SMLABB(Macros.SKP_SMLABB(out32_Q12, SA, B[Order - 2]), SB, B[Order - 1])), 12));
            S[0] = in[in_offset + k];
        }
    }
}
