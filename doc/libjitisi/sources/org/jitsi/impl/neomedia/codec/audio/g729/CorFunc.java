package org.jitsi.impl.neomedia.codec.audio.g729;

class CorFunc {
    CorFunc() {
    }

    static void corr_xy2(float[] xn, float[] y1, float[] y2, float[] g_coeff) {
        int i;
        float y2y2 = 0.01f;
        for (i = 0; i < 40; i++) {
            y2y2 += y2[i] * y2[i];
        }
        g_coeff[2] = y2y2;
        float xny2 = 0.01f;
        for (i = 0; i < 40; i++) {
            xny2 += xn[i] * y2[i];
        }
        g_coeff[3] = -2.0f * xny2;
        float y1y2 = 0.01f;
        for (i = 0; i < 40; i++) {
            y1y2 += y1[i] * y2[i];
        }
        g_coeff[4] = 2.0f * y1y2;
    }

    static void cor_h_x(float[] h, float[] x, float[] d) {
        for (int i = 0; i < 40; i++) {
            float s = 0.0f;
            for (int j = i; j < 40; j++) {
                s += x[j] * h[j - i];
            }
            d[i] = s;
        }
    }
}
