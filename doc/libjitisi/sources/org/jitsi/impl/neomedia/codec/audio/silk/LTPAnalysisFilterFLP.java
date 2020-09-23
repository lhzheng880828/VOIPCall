package org.jitsi.impl.neomedia.codec.audio.silk;

public class LTPAnalysisFilterFLP {
    static void SKP_Silk_LTP_analysis_filter_FLP(float[] LTP_res, float[] x, int x_offset, float[] B, int[] pitchL, float[] invGains, int subfr_length, int pre_length) {
        float[] Btmp = new float[5];
        float[] x_ptr = x;
        int x_ptr_offset = x_offset;
        float[] LTP_res_ptr = LTP_res;
        int LTP_res_ptr_offset = 0;
        for (int k = 0; k < 4; k++) {
            int i;
            float[] x_lag_ptr = x_ptr;
            int x_lag_ptr_offset = x_ptr_offset - pitchL[k];
            float inv_gain = invGains[k];
            for (i = 0; i < 5; i++) {
                Btmp[i] = B[(k * 5) + i];
            }
            for (i = 0; i < subfr_length + pre_length; i++) {
                int i2;
                LTP_res_ptr[LTP_res_ptr_offset + i] = x_ptr[x_ptr_offset + i];
                for (int j = 0; j < 5; j++) {
                    i2 = LTP_res_ptr_offset + i;
                    LTP_res_ptr[i2] = LTP_res_ptr[i2] - (Btmp[j] * x_lag_ptr[(x_lag_ptr_offset + 2) - j]);
                }
                i2 = LTP_res_ptr_offset + i;
                LTP_res_ptr[i2] = LTP_res_ptr[i2] * inv_gain;
                x_lag_ptr_offset++;
            }
            LTP_res_ptr_offset += subfr_length + pre_length;
            x_ptr_offset += subfr_length;
        }
    }
}
