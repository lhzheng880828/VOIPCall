package org.jitsi.impl.neomedia.codec.audio.silk;

public class Decimate2CoarsestFLP {
    static float[] A20cst_FLP = new float[]{0.28900146f};
    static float[] A21cst_FLP = new float[]{0.78048706f};

    static void SKP_Silk_decimate2_coarsest_FLP(float[] in, int in_offset, float[] S, int S_offset, float[] out, int out_offset, float[] scratch, int scratch_offset, int len) {
        int k;
        for (k = 0; k < len; k++) {
            scratch[scratch_offset + k] = in[((k * 2) + in_offset) + 0];
            scratch[(scratch_offset + k) + len] = in[((k * 2) + in_offset) + 1];
        }
        AllpassIntFLP.SKP_Silk_allpass_int_FLP(scratch, scratch_offset, S, S_offset, A21cst_FLP[0], scratch, scratch_offset + (len * 2), len);
        AllpassIntFLP.SKP_Silk_allpass_int_FLP(scratch, scratch_offset + len, S, S_offset + 1, A20cst_FLP[0], scratch, scratch_offset, len);
        for (k = 0; k < len; k++) {
            out[out_offset + k] = 0.5f * (scratch[scratch_offset + k] + scratch[(scratch_offset + k) + (len * 2)]);
        }
    }
}
