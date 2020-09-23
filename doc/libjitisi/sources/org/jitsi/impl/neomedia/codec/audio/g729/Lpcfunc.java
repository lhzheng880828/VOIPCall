package org.jitsi.impl.neomedia.codec.audio.g729;

class Lpcfunc {
    Lpcfunc() {
    }

    private static void lsp_az(float[] lsp, float[] a, int a_offset) {
        int i;
        float[] f1 = new float[6];
        float[] f2 = new float[6];
        get_lsp_pol(lsp, 0, f1);
        get_lsp_pol(lsp, 1, f2);
        for (i = 5; i > 0; i--) {
            f1[i] = f1[i] + f1[i - 1];
            f2[i] = f2[i] - f2[i - 1];
        }
        a[a_offset + 0] = 1.0f;
        i = 1;
        int j = 10;
        while (i <= 5) {
            a[a_offset + i] = (f1[i] + f2[i]) * 0.5f;
            a[a_offset + j] = (f1[i] - f2[i]) * 0.5f;
            i++;
            j--;
        }
    }

    private static void get_lsp_pol(float[] lsp, int lsp_offset, float[] f) {
        f[0] = 1.0f;
        f[1] = -2.0f * lsp[lsp_offset + 0];
        for (int i = 2; i <= 5; i++) {
            float b = -2.0f * lsp[((i * 2) + lsp_offset) - 2];
            f[i] = (f[i - 1] * b) + (2.0f * f[i - 2]);
            for (int j = i - 1; j > 1; j--) {
                f[j] = f[j] + ((f[j - 1] * b) + f[j - 2]);
            }
            f[1] = f[1] + b;
        }
    }

    static void lsf_lsp(float[] lsf, float[] lsp, int m) {
        for (int i = 0; i < m; i++) {
            lsp[i] = (float) Math.cos((double) lsf[i]);
        }
    }

    static void lsp_lsf(float[] lsp, float[] lsf, int m) {
        for (int i = 0; i < m; i++) {
            lsf[i] = (float) Math.acos((double) lsp[i]);
        }
    }

    static void weight_az(float[] a, int a_offset, float gamma, int m, float[] ap) {
        ap[0] = a[a_offset + 0];
        float fac = gamma;
        for (int i = 1; i < m; i++) {
            ap[i] = a[a_offset + i] * fac;
            fac *= gamma;
        }
        ap[m] = a[a_offset + m] * fac;
    }

    static void int_qlpc(float[] lsp_old, float[] lsp_new, float[] az) {
        float[] lsp = new float[10];
        for (int i = 0; i < 10; i++) {
            lsp[i] = (lsp_old[i] * 0.5f) + (lsp_new[i] * 0.5f);
        }
        lsp_az(lsp, az, 0);
        lsp_az(lsp_new, az, 11);
    }

    static void int_lpc(float[] lsp_old, float[] lsp_new, float[] lsf_int, float[] lsf_new, float[] az) {
        float[] lsp = new float[10];
        for (int i = 0; i < 10; i++) {
            lsp[i] = (lsp_old[i] * 0.5f) + (lsp_new[i] * 0.5f);
        }
        lsp_az(lsp, az, 0);
        lsp_lsf(lsp, lsf_int, 10);
        lsp_lsf(lsp_new, lsf_new, 10);
    }
}
