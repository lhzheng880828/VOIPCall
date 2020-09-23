package org.jitsi.impl.neomedia.codec.audio.g729;

class Lspgetq {
    Lspgetq() {
    }

    static void lsp_get_quant(float[][] lspcb1, float[][] lspcb2, int code0, int code1, int code2, float[][] fg, float[][] freq_prev, float[] lspq, float[] fg_sum) {
        int j;
        float[] buf = new float[10];
        for (j = 0; j < 5; j++) {
            buf[j] = lspcb1[code0][j] + lspcb2[code1][j];
        }
        for (j = 5; j < 10; j++) {
            buf[j] = lspcb1[code0][j] + lspcb2[code2][j];
        }
        lsp_expand_1_2(buf, 0.0012f);
        lsp_expand_1_2(buf, 6.0E-4f);
        lsp_prev_compose(buf, lspq, fg, freq_prev, fg_sum);
        lsp_prev_update(buf, freq_prev);
        lsp_stability(lspq);
    }

    static void lsp_expand_1(float[] buf, float gap) {
        for (int j = 1; j < 5; j++) {
            float tmp = ((buf[j - 1] - buf[j]) + gap) * 0.5f;
            if (tmp > 0.0f) {
                int i = j - 1;
                buf[i] = buf[i] - tmp;
                buf[j] = buf[j] + tmp;
            }
        }
    }

    static void lsp_expand_2(float[] buf, float gap) {
        for (int j = 5; j < 10; j++) {
            float tmp = ((buf[j - 1] - buf[j]) + gap) * 0.5f;
            if (tmp > 0.0f) {
                int i = j - 1;
                buf[i] = buf[i] - tmp;
                buf[j] = buf[j] + tmp;
            }
        }
    }

    static void lsp_expand_1_2(float[] buf, float gap) {
        for (int j = 1; j < 10; j++) {
            float tmp = ((buf[j - 1] - buf[j]) + gap) * 0.5f;
            if (tmp > 0.0f) {
                int i = j - 1;
                buf[i] = buf[i] - tmp;
                buf[j] = buf[j] + tmp;
            }
        }
    }

    private static void lsp_prev_compose(float[] lsp_ele, float[] lsp, float[][] fg, float[][] freq_prev, float[] fg_sum) {
        for (int j = 0; j < 10; j++) {
            lsp[j] = lsp_ele[j] * fg_sum[j];
            for (int k = 0; k < 4; k++) {
                lsp[j] = lsp[j] + (freq_prev[k][j] * fg[k][j]);
            }
        }
    }

    static void lsp_prev_extract(float[] lsp, float[] lsp_ele, float[][] fg, float[][] freq_prev, float[] fg_sum_inv) {
        for (int j = 0; j < 10; j++) {
            lsp_ele[j] = lsp[j];
            for (int k = 0; k < 4; k++) {
                lsp_ele[j] = lsp_ele[j] - (freq_prev[k][j] * fg[k][j]);
            }
            lsp_ele[j] = lsp_ele[j] * fg_sum_inv[j];
        }
    }

    static void lsp_prev_update(float[] lsp_ele, float[][] freq_prev) {
        for (int k = 4 - 1; k > 0; k--) {
            Util.copy(freq_prev[k - 1], freq_prev[k], 10);
        }
        Util.copy(lsp_ele, freq_prev[0], 10);
    }

    private static void lsp_stability(float[] buf) {
        int j;
        for (j = 0; j < 9; j++) {
            if (buf[j + 1] - buf[j] < 0.0f) {
                float tmp = buf[j + 1];
                buf[j + 1] = buf[j];
                buf[j] = tmp;
            }
        }
        if (buf[0] < 0.005f) {
            buf[0] = 0.005f;
            System.out.printf("warning LSP Low \n", new Object[0]);
        }
        for (j = 0; j < 9; j++) {
            if (buf[j + 1] - buf[j] < 0.0392f) {
                buf[j + 1] = buf[j] + 0.0392f;
            }
        }
        if (buf[9] > 3.135f) {
            buf[9] = 3.135f;
            System.out.printf("warning LSP High \n", new Object[0]);
        }
    }
}
