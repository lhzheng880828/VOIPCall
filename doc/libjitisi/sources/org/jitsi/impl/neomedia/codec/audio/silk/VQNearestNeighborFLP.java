package org.jitsi.impl.neomedia.codec.audio.silk;

public class VQNearestNeighborFLP {
    static void SKP_Silk_VQ_WMat_EC_FLP(int[] ind, int ind_offset, float[] rate_dist, float[] in, int in_offset, float[] W, int W_offset, short[] cb, short[] cl_Q6, float mu, int L) {
        float[] diff = new float[5];
        rate_dist[0] = Float.MAX_VALUE;
        short[] cb_row = cb;
        int cb_row_offset = 0;
        for (int k = 0; k < L; k++) {
            diff[0] = in[in_offset + 0] - (((float) cb_row[0]) * 6.1035E-5f);
            diff[1] = in[in_offset + 1] - (((float) cb_row[1]) * 6.1035E-5f);
            diff[2] = in[in_offset + 2] - (((float) cb_row[2]) * 6.1035E-5f);
            diff[3] = in[in_offset + 3] - (((float) cb_row[3]) * 6.1035E-5f);
            diff[4] = in[in_offset + 4] - (((float) cb_row[4]) * 6.1035E-5f);
            float sum1 = ((((((((float) cl_Q6[k]) * mu) / 64.0f) + (diff[0] * ((W[W_offset + 0] * diff[0]) + (2.0f * ((((W[W_offset + 1] * diff[1]) + (W[W_offset + 2] * diff[2])) + (W[W_offset + 3] * diff[3])) + (W[W_offset + 4] * diff[4])))))) + (diff[1] * ((W[W_offset + 6] * diff[1]) + (2.0f * (((W[W_offset + 7] * diff[2]) + (W[W_offset + 8] * diff[3])) + (W[W_offset + 9] * diff[4])))))) + (diff[2] * ((W[W_offset + 12] * diff[2]) + (2.0f * ((W[W_offset + 13] * diff[3]) + (W[W_offset + 14] * diff[4])))))) + (diff[3] * ((W[W_offset + 18] * diff[3]) + (2.0f * (W[W_offset + 19] * diff[4]))))) + (diff[4] * (W[W_offset + 24] * diff[4]));
            if (sum1 < rate_dist[0]) {
                rate_dist[0] = sum1;
                ind[ind_offset + 0] = k;
            }
            cb_row_offset += 5;
        }
    }
}
