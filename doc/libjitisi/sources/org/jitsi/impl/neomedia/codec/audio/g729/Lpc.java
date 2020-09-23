package org.jitsi.impl.neomedia.codec.audio.g729;

class Lpc {
    Lpc() {
    }

    static void autocorr(float[] x, int x_offset, int m, float[] r) {
        int i;
        float[] hamwindow = TabLd8k.hamwindow;
        float[] y = new float[240];
        for (i = 0; i < 240; i++) {
            y[i] = x[x_offset + i] * hamwindow[i];
        }
        for (i = 0; i <= m; i++) {
            float sum = 0.0f;
            for (int j = 0; j < 240 - i; j++) {
                sum += y[j] * y[j + i];
            }
            r[i] = sum;
        }
        if (r[0] < 1.0f) {
            r[0] = 1.0f;
        }
    }

    static void lag_window(int m, float[] r) {
        float[] lwindow = TabLd8k.lwindow;
        for (int i = 1; i <= m; i++) {
            r[i] = r[i] * lwindow[i - 1];
        }
    }

    static float levinson(float[] r, float[] a, int a_offset, float[] rc) {
        rc[0] = (-r[1]) / r[0];
        a[a_offset + 0] = 1.0f;
        a[a_offset + 1] = rc[0];
        float err = r[0] + (r[1] * rc[0]);
        for (int i = 2; i <= 10; i++) {
            int j;
            float s = 0.0f;
            for (j = 0; j < i; j++) {
                s += r[i - j] * a[a_offset + j];
            }
            rc[i - 1] = (-s) / err;
            for (j = 1; j <= i / 2; j++) {
                int l = i - j;
                float at = a[a_offset + j] + (rc[i - 1] * a[a_offset + l]);
                int i2 = a_offset + l;
                a[i2] = a[i2] + (rc[i - 1] * a[a_offset + j]);
                a[a_offset + j] = at;
            }
            a[a_offset + i] = rc[i - 1];
            err += rc[i - 1] * s;
            if (err <= 0.0f) {
                err = 0.001f;
            }
        }
        return err;
    }

    static void az_lsp(float[] a, int a_offset, float[] lsp, float[] old_lsp) {
        float[] grid = TabLd8k.grid;
        float[] f1 = new float[6];
        float[] f2 = new float[6];
        f1[0] = 1.0f;
        f2[0] = 1.0f;
        int i = 1;
        int j = a_offset + 10;
        while (i <= 5) {
            float ai = a[a_offset + i];
            float aj = a[j];
            f1[i] = (ai + aj) - f1[i - 1];
            f2[i] = (ai - aj) + f2[i - 1];
            i++;
            j--;
        }
        int nf = 0;
        int ip = 0;
        float[] coef = f1;
        float xlow = grid[0];
        float ylow = chebyshev(xlow, coef, 5);
        j = 0;
        while (nf < 10 && j < 60) {
            j++;
            float xhigh = xlow;
            float yhigh = ylow;
            xlow = grid[j];
            ylow = chebyshev(xlow, coef, 5);
            if (ylow * yhigh <= 0.0f) {
                j--;
                for (i = 0; i < 4; i++) {
                    float xmid = 0.5f * (xlow + xhigh);
                    float ymid = chebyshev(xmid, coef, 5);
                    if (ylow * ymid <= 0.0f) {
                        yhigh = ymid;
                        xhigh = xmid;
                    } else {
                        ylow = ymid;
                        xlow = xmid;
                    }
                }
                float xint = xlow - (((xhigh - xlow) * ylow) / (yhigh - ylow));
                lsp[nf] = xint;
                nf++;
                ip = 1 - ip;
                if (ip != 0) {
                    coef = f2;
                } else {
                    coef = f1;
                }
                xlow = xint;
                ylow = chebyshev(xlow, coef, 5);
            }
        }
        if (nf < 10) {
            for (i = 0; i < 10; i++) {
                lsp[i] = old_lsp[i];
            }
        }
    }

    private static float chebyshev(float x, float[] f, int n) {
        float x2 = 2.0f * x;
        float b2 = 1.0f;
        float b1 = x2 + f[1];
        for (int i = 2; i < n; i++) {
            b2 = b1;
            b1 = ((x2 * b1) - b2) + f[i];
        }
        return ((x * b1) - b2) + (0.5f * f[n]);
    }
}
