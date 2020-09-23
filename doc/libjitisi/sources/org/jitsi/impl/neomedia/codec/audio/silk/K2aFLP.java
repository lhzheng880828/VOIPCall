package org.jitsi.impl.neomedia.codec.audio.silk;

public class K2aFLP {
    static void SKP_Silk_k2a_FLP(float[] A, float[] rc, int order) {
        float[] Atmp = new float[16];
        for (int k = 0; k < order; k++) {
            int n;
            for (n = 0; n < k; n++) {
                Atmp[n] = A[n];
            }
            for (n = 0; n < k; n++) {
                A[n] = A[n] + (Atmp[(k - n) - 1] * rc[k]);
            }
            A[k] = -rc[k];
        }
    }
}
