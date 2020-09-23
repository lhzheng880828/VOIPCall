package org.jitsi.impl.neomedia.codec.audio.silk;

public class GainQuant {
    static final int INV_SCALE_Q16 = 1774673;
    static final int OFFSET = 2176;
    static final int SCALE_Q16 = 2420;

    static void SKP_Silk_gains_quant(int[] ind, int[] gain_Q16, int[] prev_ind, int conditional) {
        for (int k = 0; k < 4; k++) {
            ind[k] = Macros.SKP_SMULWB(SCALE_Q16, Lin2log.SKP_Silk_lin2log(gain_Q16[k]) - 2176);
            if (ind[k] < prev_ind[0]) {
                ind[k] = ind[k] + 1;
            }
            if (k == 0 && conditional == 0) {
                ind[k] = SigProcFIX.SKP_LIMIT_int(ind[k], 0, 63);
                ind[k] = Math.max(ind[k], prev_ind[0] - 4);
                prev_ind[0] = ind[k];
            } else {
                ind[k] = SigProcFIX.SKP_LIMIT_int(ind[k] - prev_ind[0], -4, 40);
                prev_ind[0] = prev_ind[0] + ind[k];
                ind[k] = ind[k] + 4;
            }
            gain_Q16[k] = Log2lin.SKP_Silk_log2lin(Math.min(Macros.SKP_SMULWB(INV_SCALE_Q16, prev_ind[0]) + OFFSET, 3967));
        }
    }

    static void SKP_Silk_gains_dequant(int[] gain_Q16, int[] ind, int[] prev_ind, int conditional) {
        for (int k = 0; k < 4; k++) {
            if (k == 0 && conditional == 0) {
                prev_ind[0] = ind[k];
            } else {
                prev_ind[0] = prev_ind[0] + (ind[k] - 4);
            }
            gain_Q16[k] = Log2lin.SKP_Silk_log2lin(Math.min(Macros.SKP_SMULWB(INV_SCALE_Q16, prev_ind[0]) + OFFSET, 3967));
        }
    }
}
