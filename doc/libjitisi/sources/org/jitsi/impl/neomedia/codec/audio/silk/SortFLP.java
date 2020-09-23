package org.jitsi.impl.neomedia.codec.audio.silk;

public class SortFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!SortFLP.class.desiredAssertionStatus());

    static void SKP_Silk_insertion_sort_increasing_FLP(float[] a, int a_offset, int[] index, int L, int K) {
        if (!$assertionsDisabled && K <= 0) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && L <= 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || L >= K) {
            int i;
            float value;
            int j;
            for (i = 0; i < K; i++) {
                index[i] = i;
            }
            for (i = 1; i < K; i++) {
                value = a[a_offset + i];
                j = i - 1;
                while (j >= 0 && value < a[a_offset + j]) {
                    a[(a_offset + j) + 1] = a[a_offset + j];
                    index[j + 1] = index[j];
                    j--;
                }
                a[(a_offset + j) + 1] = value;
                index[j + 1] = i;
            }
            for (i = K; i < L; i++) {
                value = a[a_offset + i];
                if (value < a[(a_offset + K) - 1]) {
                    j = K - 2;
                    while (j >= 0 && value < a[a_offset + j]) {
                        a[(a_offset + j) + 1] = a[a_offset + j];
                        index[j + 1] = index[j];
                        j--;
                    }
                    a[(a_offset + j) + 1] = value;
                    index[j + 1] = i;
                }
            }
        } else {
            throw new AssertionError();
        }
    }

    static void SKP_Silk_insertion_sort_decreasing_FLP(float[] a, int a_offset, int[] index, int L, int K) {
        if (!$assertionsDisabled && K <= 0) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && L <= 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || L >= K) {
            int i;
            float value;
            int j;
            for (i = 0; i < K; i++) {
                index[i] = i;
            }
            for (i = 1; i < K; i++) {
                value = a[a_offset + i];
                j = i - 1;
                while (j >= 0 && value > a[a_offset + j]) {
                    a[(a_offset + j) + 1] = a[a_offset + j];
                    index[j + 1] = index[j];
                    j--;
                }
                a[(a_offset + j) + 1] = value;
                index[j + 1] = i;
            }
            for (i = K; i < L; i++) {
                value = a[a_offset + i];
                if (value > a[(a_offset + K) - 1]) {
                    j = K - 2;
                    while (j >= 0 && value > a[a_offset + j]) {
                        a[(a_offset + j) + 1] = a[a_offset + j];
                        index[j + 1] = index[j];
                        j--;
                    }
                    a[(a_offset + j) + 1] = value;
                    index[j + 1] = i;
                }
            }
        } else {
            throw new AssertionError();
        }
    }

    static void SKP_Silk_insertion_sort_increasing_all_values_FLP(float[] a, int a_offset, int L) {
        if ($assertionsDisabled || L > 0) {
            for (int i = 1; i < L; i++) {
                float value = a[a_offset + i];
                int j = i - 1;
                while (j >= 0 && value < a[a_offset + j]) {
                    a[(a_offset + j) + 1] = a[a_offset + j];
                    j--;
                }
                a[(a_offset + j) + 1] = value;
            }
            return;
        }
        throw new AssertionError();
    }
}
