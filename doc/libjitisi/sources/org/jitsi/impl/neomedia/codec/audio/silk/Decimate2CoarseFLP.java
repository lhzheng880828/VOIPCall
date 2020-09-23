package org.jitsi.impl.neomedia.codec.audio.silk;

public class Decimate2CoarseFLP {
    static float[] A20c_FLP = new float[]{0.06466675f, 0.5085144f};
    static float[] A21c_FLP = new float[]{0.2456665f, 0.81973267f};

    static void SKP_Silk_decimate2_coarse_FLP(float[] in, int in_offset, float[] S, int S_offset, float[] out, int out_offset, float[] scratch, int scratch_offset, int len) {
        int k;
        for (k = 0; k < len; k++) {
            scratch[scratch_offset + k] = in[(k * 2) + in_offset];
            scratch[(scratch_offset + k) + len] = in[((k * 2) + in_offset) + 1];
        }
        AllpassIntFLP.SKP_Silk_allpass_int_FLP(scratch, scratch_offset, S, S_offset, A21c_FLP[0], scratch, scratch_offset + (len * 2), len);
        AllpassIntFLP.SKP_Silk_allpass_int_FLP(scratch, scratch_offset + (len * 2), S, S_offset + 1, A21c_FLP[1], scratch, scratch_offset, len);
        AllpassIntFLP.SKP_Silk_allpass_int_FLP(scratch, scratch_offset + len, S, S_offset + 2, A20c_FLP[0], scratch, scratch_offset + (len * 2), len);
        AllpassIntFLP.SKP_Silk_allpass_int_FLP(scratch, scratch_offset + (len * 2), S, S_offset + 3, A20c_FLP[1], scratch, scratch_offset + len, len);
        for (k = 0; k < len; k++) {
            out[out_offset + k] = 0.5f * (scratch[scratch_offset + k] + scratch[(scratch_offset + k) + len]);
        }
    }
}
