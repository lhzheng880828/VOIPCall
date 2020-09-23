package org.jitsi.impl.neomedia.codec.audio.silk;

public class QuantLTPGainsFLP {
    static void SKP_Silk_quant_LTP_gains_FLP(float[] B, int[] cbk_index, int[] periodicity_index, float[] W, float mu, int lowComplexity) {
        short[] cbk_ptr_Q14;
        int j;
        int[] temp_idx = new int[4];
        float rate_dist_subfr = 0.0f;
        float min_rate_dist = Float.MAX_VALUE;
        for (int k = 0; k < 3; k++) {
            int[] cdf_ptr = TablesLTP.SKP_Silk_LTP_gain_CDF_ptrs[k];
            short[] cl_ptr = TablesLTP.SKP_Silk_LTP_gain_BITS_Q6_ptrs[k];
            cbk_ptr_Q14 = TablesLTP.SKP_Silk_LTP_vq_ptrs_Q14[k];
            int cbk_size = TablesLTP.SKP_Silk_LTP_vq_sizes[k];
            float[] W_ptr = W;
            int W_ptr_offset = 0;
            float[] b_ptr = B;
            int b_ptr_offset = 0;
            float rate_dist = 0.0f;
            for (j = 0; j < 4; j++) {
                float[] rate_dist_subfr_ptr = new float[]{rate_dist_subfr};
                VQNearestNeighborFLP.SKP_Silk_VQ_WMat_EC_FLP(temp_idx, j, rate_dist_subfr_ptr, b_ptr, b_ptr_offset, W_ptr, W_ptr_offset, cbk_ptr_Q14, cl_ptr, mu, cbk_size);
                rate_dist_subfr = rate_dist_subfr_ptr[0];
                rate_dist += rate_dist_subfr;
                b_ptr_offset += 5;
                W_ptr_offset += 25;
            }
            if (rate_dist < min_rate_dist) {
                min_rate_dist = rate_dist;
                System.arraycopy(temp_idx, 0, cbk_index, 0, 4);
                periodicity_index[0] = k;
            }
            if (lowComplexity != 0 && 16384.0f * rate_dist < 11010.0f) {
                break;
            }
        }
        cbk_ptr_Q14 = TablesLTP.SKP_Silk_LTP_vq_ptrs_Q14[periodicity_index[0]];
        for (j = 0; j < 4; j++) {
            SigProcFLP.SKP_short2float_array(B, j * 5, cbk_ptr_Q14, cbk_index[j] * 5, 5);
        }
        for (j = 0; j < 20; j++) {
            B[j] = B[j] * 6.1035E-5f;
        }
    }
}
