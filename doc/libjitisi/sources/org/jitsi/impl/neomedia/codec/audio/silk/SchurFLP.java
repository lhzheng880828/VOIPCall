package org.jitsi.impl.neomedia.codec.audio.silk;

import java.lang.reflect.Array;

public class SchurFLP {
    static void SKP_Silk_schur_FLP(float[] refl_coef, int ref1_coef_offset, float[] auto_corr, int auto_corr_offset, int order) {
        int k;
        float[][] C = (float[][]) Array.newInstance(Float.TYPE, new int[]{17, 2});
        for (k = 0; k < order + 1; k++) {
            float[] fArr = C[k];
            float[] fArr2 = C[k];
            float f = auto_corr[auto_corr_offset + k];
            fArr2[1] = f;
            fArr[0] = f;
        }
        for (k = 0; k < order; k++) {
            float rc_tmp = (-C[k + 1][0]) / Math.max(C[0][1], 1.0E-9f);
            refl_coef[ref1_coef_offset + k] = rc_tmp;
            for (int n = 0; n < order - k; n++) {
                float Ctmp1 = C[(n + k) + 1][0];
                float Ctmp2 = C[n][1];
                C[(n + k) + 1][0] = (Ctmp2 * rc_tmp) + Ctmp1;
                C[n][1] = (Ctmp1 * rc_tmp) + Ctmp2;
            }
        }
    }
}
