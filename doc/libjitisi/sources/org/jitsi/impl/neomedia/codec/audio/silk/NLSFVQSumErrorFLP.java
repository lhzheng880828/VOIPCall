package org.jitsi.impl.neomedia.codec.audio.silk;

public class NLSFVQSumErrorFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!NLSFVQSumErrorFLP.class.desiredAssertionStatus());

    static void SKP_Silk_NLSF_VQ_sum_error_FLP(float[] err, float[] in, float[] w, float[] pCB, int N, int K, int LPC_order) {
        float[] Wcpy = new float[16];
        System.arraycopy(w, 0, Wcpy, 0, LPC_order);
        float[] err_tmp = err;
        int err_tmp_offset = 0;
        float[] in_tmp = in;
        int in_tmp_offset = 0;
        int n;
        float[] cb_vec;
        int cb_vec_offset;
        int i;
        float diff;
        float sum_error;
        if (LPC_order == 16) {
            for (n = 0; n < N; n++) {
                cb_vec = pCB;
                cb_vec_offset = 0;
                for (i = 0; i < K; i++) {
                    diff = in_tmp[in_tmp_offset + 0] - cb_vec[cb_vec_offset + 0];
                    sum_error = (Wcpy[0] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 1] - cb_vec[cb_vec_offset + 1];
                    sum_error += (Wcpy[1] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 2] - cb_vec[cb_vec_offset + 2];
                    sum_error += (Wcpy[2] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 3] - cb_vec[cb_vec_offset + 3];
                    sum_error += (Wcpy[3] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 4] - cb_vec[cb_vec_offset + 4];
                    sum_error += (Wcpy[4] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 5] - cb_vec[cb_vec_offset + 5];
                    sum_error += (Wcpy[5] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 6] - cb_vec[cb_vec_offset + 6];
                    sum_error += (Wcpy[6] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 7] - cb_vec[cb_vec_offset + 7];
                    sum_error += (Wcpy[7] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 8] - cb_vec[cb_vec_offset + 8];
                    sum_error += (Wcpy[8] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 9] - cb_vec[cb_vec_offset + 9];
                    sum_error += (Wcpy[9] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 10] - cb_vec[cb_vec_offset + 10];
                    sum_error += (Wcpy[10] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 11] - cb_vec[cb_vec_offset + 11];
                    sum_error += (Wcpy[11] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 12] - cb_vec[cb_vec_offset + 12];
                    sum_error += (Wcpy[12] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 13] - cb_vec[cb_vec_offset + 13];
                    sum_error += (Wcpy[13] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 14] - cb_vec[cb_vec_offset + 14];
                    sum_error += (Wcpy[14] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 15] - cb_vec[cb_vec_offset + 15];
                    err_tmp[err_tmp_offset + i] = sum_error + ((Wcpy[15] * diff) * diff);
                    cb_vec_offset += 16;
                }
                err_tmp_offset += K;
                in_tmp_offset += 16;
            }
        } else if ($assertionsDisabled || LPC_order == 10) {
            for (n = 0; n < N; n++) {
                cb_vec = pCB;
                cb_vec_offset = 0;
                for (i = 0; i < K; i++) {
                    diff = in_tmp[in_tmp_offset + 0] - cb_vec[cb_vec_offset + 0];
                    sum_error = (Wcpy[0] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 1] - cb_vec[cb_vec_offset + 1];
                    sum_error += (Wcpy[1] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 2] - cb_vec[cb_vec_offset + 2];
                    sum_error += (Wcpy[2] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 3] - cb_vec[cb_vec_offset + 3];
                    sum_error += (Wcpy[3] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 4] - cb_vec[cb_vec_offset + 4];
                    sum_error += (Wcpy[4] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 5] - cb_vec[cb_vec_offset + 5];
                    sum_error += (Wcpy[5] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 6] - cb_vec[cb_vec_offset + 6];
                    sum_error += (Wcpy[6] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 7] - cb_vec[cb_vec_offset + 7];
                    sum_error += (Wcpy[7] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 8] - cb_vec[cb_vec_offset + 8];
                    sum_error += (Wcpy[8] * diff) * diff;
                    diff = in_tmp[in_tmp_offset + 9] - cb_vec[cb_vec_offset + 9];
                    err_tmp[err_tmp_offset + i] = sum_error + ((Wcpy[9] * diff) * diff);
                    cb_vec_offset += 10;
                }
                err_tmp_offset += K;
                in_tmp_offset += 10;
            }
        } else {
            throw new AssertionError();
        }
    }
}
