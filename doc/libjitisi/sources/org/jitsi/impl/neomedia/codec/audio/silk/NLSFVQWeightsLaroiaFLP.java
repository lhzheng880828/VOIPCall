package org.jitsi.impl.neomedia.codec.audio.silk;

public class NLSFVQWeightsLaroiaFLP {
    static final /* synthetic */ boolean $assertionsDisabled = (!NLSFVQWeightsLaroiaFLP.class.desiredAssertionStatus());
    static float MIN_NDELTA = 3.1830987E-7f;

    static void SKP_Silk_NLSF_VQ_weights_laroia_FLP(float[] pXW, float[] pX, int D) {
        if (!$assertionsDisabled && D <= 0) {
            throw new AssertionError();
        } else if ($assertionsDisabled || (D & 1) == 0) {
            float tmp2 = 1.0f / (pX[1] - pX[0] > MIN_NDELTA ? pX[1] - pX[0] : MIN_NDELTA);
            pXW[0] = (1.0f / (pX[0] > MIN_NDELTA ? pX[0] : MIN_NDELTA)) + tmp2;
            for (int k = 1; k < D - 1; k += 2) {
                float tmp1 = 1.0f / (pX[k + 1] - pX[k] > MIN_NDELTA ? pX[k + 1] - pX[k] : MIN_NDELTA);
                pXW[k] = tmp1 + tmp2;
                tmp2 = 1.0f / (pX[k + 2] - pX[k + 1] > MIN_NDELTA ? pX[k + 2] - pX[k + 1] : MIN_NDELTA);
                pXW[k + 1] = tmp1 + tmp2;
            }
            pXW[D - 1] = (1.0f / (1.0f - pX[D + -1] > MIN_NDELTA ? 1.0f - pX[D - 1] : MIN_NDELTA)) + tmp2;
        } else {
            throw new AssertionError();
        }
    }
}
