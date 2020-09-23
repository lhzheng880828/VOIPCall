package org.jitsi.impl.neomedia.codec.audio.g729;

import java.lang.reflect.Array;

class Lspdec {
    private static final float[] FREQ_PREV_RESET = new float[]{0.285599f, 0.571199f, 0.856798f, 1.142397f, 1.427997f, 1.713596f, 1.999195f, 2.284795f, 2.570394f, 2.855993f};
    private static final int M = 10;
    private static final int MA_NP = 4;
    private final float[][] freq_prev = ((float[][]) Array.newInstance(Float.TYPE, new int[]{4, 10}));
    private final float[] prev_lsp = new float[10];
    private int prev_ma;

    Lspdec() {
    }

    /* access modifiers changed from: 0000 */
    public void lsp_decw_reset() {
        for (int i = 0; i < 4; i++) {
            Util.copy(FREQ_PREV_RESET, this.freq_prev[i], 10);
        }
        this.prev_ma = 0;
        Util.copy(FREQ_PREV_RESET, this.prev_lsp, 10);
    }

    private void lsp_iqua_cs(int[] prm, int prm_offset, float[] lsp_q, int erase) {
        float[][][] fg = TabLd8k.fg;
        float[][] fg_sum = TabLd8k.fg_sum;
        float[][] fg_sum_inv = TabLd8k.fg_sum_inv;
        float[][] lspcb1 = TabLd8k.lspcb1;
        float[][] lspcb2 = TabLd8k.lspcb2;
        float[] buf = new float[10];
        if (erase == 0) {
            int mode_index = (prm[prm_offset + 0] >>> 7) & 1;
            Lspgetq.lsp_get_quant(lspcb1, lspcb2, prm[prm_offset + 0] & ((short) 127), (prm[prm_offset + 1] >>> 5) & ((short) 31), prm[prm_offset + 1] & ((short) 31), fg[mode_index], this.freq_prev, lsp_q, fg_sum[mode_index]);
            Util.copy(lsp_q, this.prev_lsp, 10);
            this.prev_ma = mode_index;
            return;
        }
        Util.copy(this.prev_lsp, lsp_q, 10);
        Lspgetq.lsp_prev_extract(this.prev_lsp, buf, fg[this.prev_ma], this.freq_prev, fg_sum_inv[this.prev_ma]);
        Lspgetq.lsp_prev_update(buf, this.freq_prev);
    }

    /* access modifiers changed from: 0000 */
    public void d_lsp(int[] index, int index_offset, float[] lsp_q, int bfi) {
        lsp_iqua_cs(index, index_offset, lsp_q, bfi);
        for (int i = 0; i < 10; i++) {
            lsp_q[i] = (float) Math.cos((double) lsp_q[i]);
        }
    }
}
