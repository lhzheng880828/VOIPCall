package org.jitsi.impl.neomedia.codec.audio.g729;

class Pwf {
    private final float[] lar_old = new float[]{0.0f, 0.0f};
    private int smooth = 1;

    Pwf() {
    }

    /* access modifiers changed from: 0000 */
    public void perc_var(float[] gamma1, float[] gamma2, float[] lsfint, float[] lsfnew, float[] r_c) {
        int i;
        float[] lar = new float[4];
        float[] lar_new = lar;
        for (i = 0; i < 2; i++) {
            lar_new[2 + i] = (float) Math.log10((double) ((1.0f + r_c[i]) / (1.0f - r_c[i])));
        }
        for (i = 0; i < 2; i++) {
            lar[i] = 0.5f * (lar_new[2 + i] + this.lar_old[i]);
            this.lar_old[i] = lar_new[2 + i];
        }
        for (int k = 0; k < 2; k++) {
            float critlar0 = lar[k * 2];
            float critlar1 = lar[(k * 2) + 1];
            if (this.smooth != 0) {
                if (critlar0 < -1.74f && critlar1 > 0.65f) {
                    this.smooth = 0;
                }
            } else if (critlar0 > -1.52f || critlar1 < 0.43f) {
                this.smooth = 1;
            }
            if (this.smooth == 0) {
                float[] lsf;
                gamma1[k] = 0.98f;
                if (k == 0) {
                    lsf = lsfint;
                } else {
                    lsf = lsfnew;
                }
                float d_min = lsf[1] - lsf[0];
                for (i = 1; i < 9; i++) {
                    float temp = lsf[i + 1] - lsf[i];
                    if (temp < d_min) {
                        d_min = temp;
                    }
                }
                gamma2[k] = (-6.0f * d_min) + 1.0f;
                if (gamma2[k] > 0.7f) {
                    gamma2[k] = 0.7f;
                }
                if (gamma2[k] < 0.4f) {
                    gamma2[k] = 0.4f;
                }
            } else {
                gamma1[k] = 0.94f;
                gamma2[k] = 0.6f;
            }
        }
    }
}
