package org.jitsi.impl.neomedia.codec.audio.silk;

import java.lang.reflect.Array;

public class LPCInvPredGainFLP {
    static final float RC_THRESHOLD = 0.9999f;

    static int SKP_Silk_LPC_inverse_pred_gain_FLP(float[] invGain, float[] A, int A_offset, int order) {
        double rc;
        float[][] Atmp = (float[][]) Array.newInstance(Float.TYPE, new int[]{2, 16});
        float[] Anew = Atmp[order & 1];
        for (int i_djinn = 0; i_djinn < order; i_djinn++) {
            Anew[i_djinn] = A[A_offset + i_djinn];
        }
        invGain[0] = 1.0f;
        for (int k = order - 1; k > 0; k--) {
            rc = (double) (-Anew[k]);
            if (rc > 0.9998999834060669d || rc < -0.9998999834060669d) {
                return 1;
            }
            double rc_mult1 = 1.0d - (rc * rc);
            double rc_mult2 = 1.0d / rc_mult1;
            invGain[0] = invGain[0] * ((float) rc_mult1);
            float[] Aold = Anew;
            Anew = Atmp[k & 1];
            for (int n = 0; n < k; n++) {
                Anew[n] = (float) ((((double) Aold[n]) - (((double) Aold[(k - n) - 1]) * rc)) * rc_mult2);
            }
        }
        rc = (double) (-Anew[0]);
        if (rc > 0.9998999834060669d || rc < -0.9998999834060669d) {
            return 1;
        }
        invGain[0] = invGain[0] * ((float) (1.0d - (rc * rc)));
        return 0;
    }
}
