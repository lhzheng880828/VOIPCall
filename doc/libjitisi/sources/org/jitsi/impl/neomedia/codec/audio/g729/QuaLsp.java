package org.jitsi.impl.neomedia.codec.audio.g729;

import java.lang.reflect.Array;

class QuaLsp {
    private static final float[] FREQ_PREV_RESET = new float[]{0.285599f, 0.571199f, 0.856798f, 1.142397f, 1.427997f, 1.713596f, 1.999195f, 2.284795f, 2.570394f, 2.855993f};
    private final float[][] freq_prev = ((float[][]) Array.newInstance(Float.TYPE, new int[]{4, 10}));

    QuaLsp() {
    }

    /* access modifiers changed from: 0000 */
    public void qua_lsp(float[] lsp, float[] lsp_q, int[] ana) {
        int i;
        float[] lsf = new float[10];
        float[] lsf_q = new float[10];
        for (i = 0; i < 10; i++) {
            lsf[i] = (float) Math.acos((double) lsp[i]);
        }
        lsp_qua_cs(lsf, lsf_q, ana);
        for (i = 0; i < 10; i++) {
            lsp_q[i] = (float) Math.cos((double) lsf_q[i]);
        }
    }

    /* access modifiers changed from: 0000 */
    public void lsp_encw_reset() {
        for (int i = 0; i < 4; i++) {
            Util.copy(FREQ_PREV_RESET, this.freq_prev[i], 10);
        }
    }

    private void lsp_qua_cs(float[] flsp_in, float[] lspq_out, int[] code) {
        float[][][] fg = TabLd8k.fg;
        float[][] fg_sum = TabLd8k.fg_sum;
        float[][] fg_sum_inv = TabLd8k.fg_sum_inv;
        float[][] lspcb1 = TabLd8k.lspcb1;
        float[][] lspcb2 = TabLd8k.lspcb2;
        float[] wegt = new float[10];
        get_wegt(flsp_in, wegt);
        relspwed(flsp_in, wegt, lspq_out, lspcb1, lspcb2, fg, this.freq_prev, fg_sum, fg_sum_inv, code);
    }

    private void relspwed(float[] lsp, float[] wegt, float[] lspq, float[][] lspcb1, float[][] lspcb2, float[][][] fg, float[][] freq_prev, float[][] fg_sum, float[][] fg_sum_inv, int[] code_ana) {
        int[] cand = new int[2];
        int[] tindex1 = new int[2];
        int[] tindex2 = new int[2];
        float[] tdist = new float[2];
        float[] rbuf = new float[10];
        float[] buf = new float[10];
        for (int mode = 0; mode < 2; mode++) {
            int j;
            Lspgetq.lsp_prev_extract(lsp, rbuf, fg[mode], freq_prev, fg_sum_inv[mode]);
            int cand_cur = lsp_pre_select(rbuf, lspcb1);
            cand[mode] = cand_cur;
            int index = lsp_select_1(rbuf, lspcb1[cand_cur], wegt, lspcb2);
            tindex1[mode] = index;
            for (j = 0; j < 5; j++) {
                buf[j] = lspcb1[cand_cur][j] + lspcb2[index][j];
            }
            Lspgetq.lsp_expand_1(buf, 0.0012f);
            index = lsp_select_2(rbuf, lspcb1[cand_cur], wegt, lspcb2);
            tindex2[mode] = index;
            for (j = 5; j < 10; j++) {
                buf[j] = lspcb1[cand_cur][j] + lspcb2[index][j];
            }
            Lspgetq.lsp_expand_2(buf, 0.0012f);
            Lspgetq.lsp_expand_1_2(buf, 6.0E-4f);
            tdist[mode] = lsp_get_tdist(wegt, buf, rbuf, fg_sum[mode]);
        }
        int mode_index = lsp_last_select(tdist);
        code_ana[0] = (mode_index << 7) | cand[mode_index];
        code_ana[1] = (tindex1[mode_index] << 5) | tindex2[mode_index];
        Lspgetq.lsp_get_quant(lspcb1, lspcb2, cand[mode_index], tindex1[mode_index], tindex2[mode_index], fg[mode_index], freq_prev, lspq, fg_sum[mode_index]);
    }

    private int lsp_pre_select(float[] rbuf, float[][] lspcb1) {
        int cand = 0;
        float dmin = 1.0E38f;
        for (int i = 0; i < 128; i++) {
            float dist = 0.0f;
            for (int j = 0; j < 10; j++) {
                float temp = rbuf[j] - lspcb1[i][j];
                dist += temp * temp;
            }
            if (dist < dmin) {
                dmin = dist;
                cand = i;
            }
        }
        return cand;
    }

    private int lsp_select_1(float[] rbuf, float[] lspcb1, float[] wegt, float[][] lspcb2) {
        int j;
        float[] buf = new float[10];
        for (j = 0; j < 5; j++) {
            buf[j] = rbuf[j] - lspcb1[j];
        }
        int index = 0;
        float dmin = 1.0E38f;
        for (int k1 = 0; k1 < 32; k1++) {
            float dist = 0.0f;
            for (j = 0; j < 5; j++) {
                float tmp = buf[j] - lspcb2[k1][j];
                dist += (wegt[j] * tmp) * tmp;
            }
            if (dist < dmin) {
                dmin = dist;
                index = k1;
            }
        }
        return index;
    }

    private int lsp_select_2(float[] rbuf, float[] lspcb1, float[] wegt, float[][] lspcb2) {
        int j;
        float[] buf = new float[10];
        for (j = 5; j < 10; j++) {
            buf[j] = rbuf[j] - lspcb1[j];
        }
        int index = 0;
        float dmin = 1.0E38f;
        for (int k1 = 0; k1 < 32; k1++) {
            float dist = 0.0f;
            for (j = 5; j < 10; j++) {
                float tmp = buf[j] - lspcb2[k1][j];
                dist += (wegt[j] * tmp) * tmp;
            }
            if (dist < dmin) {
                dmin = dist;
                index = k1;
            }
        }
        return index;
    }

    private float lsp_get_tdist(float[] wegt, float[] buf, float[] rbuf, float[] fg_sum) {
        float tdist = 0.0f;
        for (int j = 0; j < 10; j++) {
            float tmp = (buf[j] - rbuf[j]) * fg_sum[j];
            tdist += (wegt[j] * tmp) * tmp;
        }
        return tdist;
    }

    private int lsp_last_select(float[] tdist) {
        if (tdist[1] < tdist[0]) {
            return 1;
        }
        return 0;
    }

    private void get_wegt(float[] flsp, float[] wegt) {
        float tmp = (flsp[1] - 0.12566371f) - 1.0f;
        if (tmp > 0.0f) {
            wegt[0] = 1.0f;
        } else {
            wegt[0] = ((tmp * tmp) * 10.0f) + 1.0f;
        }
        for (int i = 1; i < 9; i++) {
            tmp = (flsp[i + 1] - flsp[i - 1]) - 1.0f;
            if (tmp > 0.0f) {
                wegt[i] = 1.0f;
            } else {
                wegt[i] = ((tmp * tmp) * 10.0f) + 1.0f;
            }
        }
        tmp = (2.8902655f - flsp[8]) - 1.0f;
        if (tmp > 0.0f) {
            wegt[9] = 1.0f;
        } else {
            wegt[9] = ((tmp * tmp) * 10.0f) + 1.0f;
        }
        wegt[4] = wegt[4] * 1.2f;
        wegt[5] = wegt[5] * 1.2f;
    }
}
