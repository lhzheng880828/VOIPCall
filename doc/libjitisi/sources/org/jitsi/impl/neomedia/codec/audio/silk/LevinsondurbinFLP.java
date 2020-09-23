package org.jitsi.impl.neomedia.codec.audio.silk;

public class LevinsondurbinFLP {
    static float SKP_Silk_levinsondurbin_FLP(float[] A, int A_offset, float[] corr, int order) {
        float min_nrg = (1.0E-12f * corr[0]) + 1.0E-9f;
        float nrg = Math.max(min_nrg, corr[0]);
        A[A_offset] = corr[1] / nrg;
        nrg = Math.max(min_nrg, nrg - (A[A_offset] * corr[1]));
        for (int m = 1; m < order; m++) {
            int i;
            int i2;
            float t = corr[m + 1];
            for (i = 0; i < m; i++) {
                t -= A[A_offset + i] * corr[m - i];
            }
            float km = t / nrg;
            nrg = Math.max(min_nrg, nrg - (km * t));
            int mHalf = m >> 1;
            for (i = 0; i < mHalf; i++) {
                float Atmp1 = A[A_offset + i];
                float Atmp2 = A[((A_offset + m) - i) - 1];
                i2 = ((A_offset + m) - i) - 1;
                A[i2] = A[i2] - (km * Atmp1);
                i2 = A_offset + i;
                A[i2] = A[i2] - (km * Atmp2);
            }
            if ((m & 1) != 0) {
                i2 = A_offset + mHalf;
                A[i2] = A[i2] - (A[A_offset + mHalf] * km);
            }
            A[A_offset + m] = km;
        }
        return nrg;
    }
}
