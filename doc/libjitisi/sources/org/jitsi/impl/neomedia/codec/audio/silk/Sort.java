package org.jitsi.impl.neomedia.codec.audio.silk;

public class Sort {
    static final /* synthetic */ boolean $assertionsDisabled = (!Sort.class.desiredAssertionStatus());

    static void SKP_Silk_insertion_sort_increasing(int[] a, int[] index, int L, int K) {
        if (!$assertionsDisabled && K <= 0) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && L <= 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || L >= K) {
            int i;
            int value;
            int j;
            for (i = 0; i < K; i++) {
                index[i] = i;
            }
            for (i = 1; i < K; i++) {
                value = a[i];
                j = i - 1;
                while (j >= 0 && value < a[j]) {
                    a[j + 1] = a[j];
                    index[j + 1] = index[j];
                    j--;
                }
                a[j + 1] = value;
                index[j + 1] = i;
            }
            for (i = K; i < L; i++) {
                value = a[i];
                if (value < a[K - 1]) {
                    j = K - 2;
                    while (j >= 0 && value < a[j]) {
                        a[j + 1] = a[j];
                        index[j + 1] = index[j];
                        j--;
                    }
                    a[j + 1] = value;
                    index[j + 1] = i;
                }
            }
        } else {
            throw new AssertionError();
        }
    }

    static void SKP_Silk_insertion_sort_decreasing(int[] a, int[] index, int L, int K) {
        if (!$assertionsDisabled && K <= 0) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && L <= 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || L >= K) {
            int i;
            int value;
            int j;
            for (i = 0; i < K; i++) {
                index[i] = i;
            }
            for (i = 1; i < K; i++) {
                value = a[i];
                j = i - 1;
                while (j >= 0 && value > a[j]) {
                    a[j + 1] = a[j];
                    index[j + 1] = index[j];
                    j--;
                }
                a[j + 1] = value;
                index[j + 1] = i;
            }
            for (i = K; i < L; i++) {
                value = a[i];
                if (value > a[K - 1]) {
                    j = K - 2;
                    while (j >= 0 && value > a[j]) {
                        a[j + 1] = a[j];
                        index[j + 1] = index[j];
                        j--;
                    }
                    a[j + 1] = value;
                    index[j + 1] = i;
                }
            }
        } else {
            throw new AssertionError();
        }
    }

    static void SKP_Silk_insertion_sort_decreasing_int16(short[] a, int[] index, int L, int K) {
        if (!$assertionsDisabled && K <= 0) {
            throw new AssertionError();
        } else if (!$assertionsDisabled && L <= 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || L >= K) {
            int i;
            short value;
            int j;
            for (i = 0; i < K; i++) {
                index[i] = i;
            }
            for (i = 1; i < K; i++) {
                value = a[i];
                j = i - 1;
                while (j >= 0 && value > a[j]) {
                    a[j + 1] = a[j];
                    index[j + 1] = index[j];
                    j--;
                }
                a[j + 1] = (short) value;
                index[j + 1] = i;
            }
            for (i = K; i < L; i++) {
                value = a[i];
                if (value > a[K - 1]) {
                    j = K - 2;
                    while (j >= 0 && value > a[j]) {
                        a[j + 1] = a[j];
                        index[j + 1] = index[j];
                        j--;
                    }
                    a[j + 1] = (short) value;
                    index[j + 1] = i;
                }
            }
        } else {
            throw new AssertionError();
        }
    }

    static void SKP_Silk_insertion_sort_increasing_all_values(int[] a, int a_offset, int L) {
        Typedef.SKP_assert(L > 0);
        for (int i = 1; i < L; i++) {
            int value = a[a_offset + i];
            int j = i - 1;
            while (j >= 0 && value < a[a_offset + j]) {
                a[(a_offset + j) + 1] = a[a_offset + j];
                j--;
            }
            a[(a_offset + j) + 1] = value;
        }
    }

    static void SKP_Silk_shell_insertion_sort_increasing(int[] a, int[] index, int L, int K) {
        boolean z;
        int i;
        int value;
        int j;
        boolean z2 = true;
        if (K > 0) {
            z = true;
        } else {
            z = false;
        }
        Typedef.SKP_assert(z);
        if (L > 0) {
            z = true;
        } else {
            z = false;
        }
        Typedef.SKP_assert(z);
        if (L < K) {
            z2 = false;
        }
        Typedef.SKP_assert(z2);
        int inc_Q16_tmp = L << 15;
        int inc = inc_Q16_tmp >> 16;
        for (i = 0; i < K; i++) {
            index[i] = i;
        }
        while (inc > 0) {
            for (i = inc; i < K; i++) {
                value = a[i];
                int idx = index[i];
                j = i - inc;
                while (j >= 0 && value < a[j]) {
                    a[j + inc] = a[j];
                    index[j + inc] = index[j];
                    j -= inc;
                }
                a[j + inc] = value;
                index[j + inc] = idx;
            }
            inc_Q16_tmp = Macros.SKP_SMULWB(inc_Q16_tmp, 29789);
            inc = SigProcFIX.SKP_RSHIFT_ROUND(inc_Q16_tmp, 16);
        }
        for (i = K; i < L; i++) {
            value = a[i];
            if (value < a[K - 1]) {
                j = K - 2;
                while (j >= 0 && value < a[j]) {
                    a[j + 1] = a[j];
                    index[j + 1] = index[j];
                    j--;
                }
                a[j + 1] = value;
                index[j + 1] = i;
            }
        }
    }

    static void SKP_Silk_shell_sort_increasing_all_values(int[] a, int[] index, int L) {
        int i;
        Typedef.SKP_assert(L > 0);
        int inc_Q16_tmp = L << 15;
        int inc = inc_Q16_tmp >> 16;
        for (i = 0; i < L; i++) {
            index[i] = i;
        }
        while (inc > 0) {
            for (i = inc; i < L; i++) {
                int value = a[i];
                int idx = index[i];
                int j = i - inc;
                while (j >= 0 && value < a[j]) {
                    a[j + inc] = a[j];
                    index[j + inc] = index[j];
                    j -= inc;
                }
                a[j + inc] = value;
                index[j + inc] = idx;
            }
            inc_Q16_tmp = Macros.SKP_SMULWB(inc_Q16_tmp, 29789);
            inc = SigProcFIX.SKP_RSHIFT_ROUND(inc_Q16_tmp, 16);
        }
    }
}
