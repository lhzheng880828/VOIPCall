package org.jitsi.impl.neomedia.codec.audio.silk;

public class SigmQ15 {
    static int[] sigm_LUT_neg_Q15 = new int[]{16384, 8812, 3906, 1554, 589, 219};
    static int[] sigm_LUT_pos_Q15 = new int[]{16384, 23955, 28861, 31213, 32178, 32548};
    static int[] sigm_LUT_slope_Q10 = new int[]{237, 153, 73, 30, 12, 7};

    static int SKP_Silk_sigm_Q15(int in_Q5) {
        int ind;
        if (in_Q5 < 0) {
            in_Q5 = -in_Q5;
            if (in_Q5 >= 192) {
                return 0;
            }
            ind = in_Q5 >> 5;
            return sigm_LUT_neg_Q15[ind] - Macros.SKP_SMULBB(sigm_LUT_slope_Q10[ind], in_Q5 & 31);
        } else if (in_Q5 >= 192) {
            return 32767;
        } else {
            ind = in_Q5 >> 5;
            return sigm_LUT_pos_Q15[ind] + Macros.SKP_SMULBB(sigm_LUT_slope_Q10[ind], in_Q5 & 31);
        }
    }
}
