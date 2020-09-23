package org.jitsi.impl.neomedia.codec.audio.silk;

public class NLSFStabilize {
    static final int MAX_LOOPS = 20;

    static void SKP_Silk_NLSF_stabilize(int[] NLSF_Q15, int NLSF_Q15_offset, int[] NDeltaMin_Q15, int L) {
        int i;
        Typedef.SKP_assert(NDeltaMin_Q15[L] >= 1);
        int loops = 0;
        while (loops < 20) {
            int diff_Q15;
            int min_diff_Q15 = NLSF_Q15[NLSF_Q15_offset + 0] - NDeltaMin_Q15[0];
            int I = 0;
            for (i = 1; i <= L - 1; i++) {
                diff_Q15 = NLSF_Q15[NLSF_Q15_offset + i] - (NLSF_Q15[(NLSF_Q15_offset + i) - 1] + NDeltaMin_Q15[i]);
                if (diff_Q15 < min_diff_Q15) {
                    min_diff_Q15 = diff_Q15;
                    I = i;
                }
            }
            diff_Q15 = 32768 - (NLSF_Q15[(NLSF_Q15_offset + L) - 1] + NDeltaMin_Q15[L]);
            if (diff_Q15 < min_diff_Q15) {
                min_diff_Q15 = diff_Q15;
                I = L;
            }
            if (min_diff_Q15 < 0) {
                if (I == 0) {
                    NLSF_Q15[NLSF_Q15_offset + 0] = NDeltaMin_Q15[0];
                } else if (I == L) {
                    NLSF_Q15[(NLSF_Q15_offset + L) - 1] = 32768 - NDeltaMin_Q15[L];
                } else {
                    int k;
                    int min_center_Q15 = 0;
                    for (k = 0; k < I; k++) {
                        min_center_Q15 += NDeltaMin_Q15[k];
                    }
                    min_center_Q15 += NDeltaMin_Q15[I] >> 1;
                    int max_center_Q15 = 32768;
                    for (k = L; k > I; k--) {
                        max_center_Q15 -= NDeltaMin_Q15[k];
                    }
                    NLSF_Q15[(NLSF_Q15_offset + I) - 1] = SigProcFIX.SKP_LIMIT_32(SigProcFIX.SKP_RSHIFT_ROUND(NLSF_Q15[(NLSF_Q15_offset + I) - 1] + NLSF_Q15[NLSF_Q15_offset + I], 1), min_center_Q15, max_center_Q15 - (NDeltaMin_Q15[I] - (NDeltaMin_Q15[I] >> 1))) - (NDeltaMin_Q15[I] >> 1);
                    NLSF_Q15[NLSF_Q15_offset + I] = NLSF_Q15[(NLSF_Q15_offset + I) - 1] + NDeltaMin_Q15[I];
                }
                loops++;
            } else {
                return;
            }
        }
        if (loops == 20) {
            Sort.SKP_Silk_insertion_sort_increasing_all_values(NLSF_Q15, NLSF_Q15_offset + 0, L);
            NLSF_Q15[NLSF_Q15_offset + 0] = SigProcFIX.SKP_max_int(NLSF_Q15[NLSF_Q15_offset + 0], NDeltaMin_Q15[0]);
            for (i = 1; i < L; i++) {
                NLSF_Q15[NLSF_Q15_offset + i] = SigProcFIX.SKP_max_int(NLSF_Q15[NLSF_Q15_offset + i], NLSF_Q15[(NLSF_Q15_offset + i) - 1] + NDeltaMin_Q15[i]);
            }
            NLSF_Q15[(NLSF_Q15_offset + L) - 1] = SigProcFIX.SKP_min_int(NLSF_Q15[(NLSF_Q15_offset + L) - 1], 32768 - NDeltaMin_Q15[L]);
            for (i = L - 2; i >= 0; i--) {
                NLSF_Q15[NLSF_Q15_offset + i] = SigProcFIX.SKP_min_int(NLSF_Q15[NLSF_Q15_offset + i], NLSF_Q15[(NLSF_Q15_offset + i) + 1] - NDeltaMin_Q15[i + 1]);
            }
        }
    }

    static void SKP_Silk_NLSF_stabilize_multi(int[] NLSF_Q15, int[] NDeltaMin_Q15, int N, int L) {
        for (int n = 0; n < N; n++) {
            SKP_Silk_NLSF_stabilize(NLSF_Q15, n * L, NDeltaMin_Q15, L);
        }
    }
}
