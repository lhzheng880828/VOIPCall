package org.jitsi.impl.neomedia.codec.audio.g729;

class DecGain {
    private final float[] past_qua_en = new float[]{-14.0f, -14.0f, -14.0f, -14.0f};

    DecGain() {
    }

    /* access modifiers changed from: 0000 */
    public void dec_gain(int index, float[] code, int l_subfr, int bfi, FloatReference gain_pit, FloatReference gain_code) {
        float[][] gbk1 = TabLd8k.gbk1;
        float[][] gbk2 = TabLd8k.gbk2;
        int[] imap1 = TabLd8k.imap1;
        int[] imap2 = TabLd8k.imap2;
        if (bfi != 0) {
            gain_pit.value *= 0.9f;
            if (gain_pit.value > 0.9f) {
                gain_pit.value = 0.9f;
            }
            gain_code.value *= 0.98f;
            Gainpred.gain_update_erasure(this.past_qua_en);
            return;
        }
        int index1 = imap1[index / 16];
        int index2 = imap2[index % 16];
        gain_pit.value = gbk1[index1][0] + gbk2[index2][0];
        float g_code = gbk1[index1][1] + gbk2[index2][1];
        gain_code.value = g_code * Gainpred.gain_predict(this.past_qua_en, code, l_subfr);
        Gainpred.gain_update(this.past_qua_en, g_code);
    }
}
