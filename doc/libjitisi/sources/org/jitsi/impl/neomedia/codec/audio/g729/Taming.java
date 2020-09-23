package org.jitsi.impl.neomedia.codec.audio.g729;

class Taming {
    private final float[] exc_err = new float[4];

    Taming() {
    }

    /* access modifiers changed from: 0000 */
    public void init_exc_err() {
        for (int i = 0; i < 4; i++) {
            this.exc_err[i] = 1.0f;
        }
    }

    /* access modifiers changed from: 0000 */
    public int test_err(int t0, int t0_frac) {
        int t1;
        if (t0_frac > 0) {
            t1 = t0 + 1;
        } else {
            t1 = t0;
        }
        int i = (t1 - 40) - 10;
        if (i < 0) {
            i = 0;
        }
        int zone1 = (int) (((float) i) * 0.025f);
        float maxloc = -1.0f;
        for (i = (int) (((float) ((t1 + 10) - 2)) * 0.025f); i >= zone1; i--) {
            if (this.exc_err[i] > maxloc) {
                maxloc = this.exc_err[i];
            }
        }
        if (maxloc > 60000.0f) {
            return 1;
        }
        return 0;
    }

    /* access modifiers changed from: 0000 */
    public void update_exc_err(float gain_pit, int t0) {
        int i;
        float worst = -1.0f;
        int n = t0 - 40;
        float temp;
        if (n < 0) {
            temp = 1.0f + (this.exc_err[0] * gain_pit);
            if (temp > -1.0f) {
                worst = temp;
            }
            temp = 1.0f + (gain_pit * temp);
            if (temp > worst) {
                worst = temp;
            }
        } else {
            int zone2 = (int) (((float) (t0 - 1)) * 0.025f);
            for (i = (int) (((float) n) * 0.025f); i <= zone2; i++) {
                temp = 1.0f + (this.exc_err[i] * gain_pit);
                if (temp > worst) {
                    worst = temp;
                }
            }
        }
        for (i = 3; i >= 1; i--) {
            this.exc_err[i] = this.exc_err[i - 1];
        }
        this.exc_err[0] = worst;
    }
}
