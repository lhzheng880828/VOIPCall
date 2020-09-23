package org.jitsi.impl.neomedia.codec.audio.g729;

class Gainpred {
    Gainpred() {
    }

    static float gain_predict(float[] past_qua_en, float[] code, int l_subfr) {
        int i;
        float[] pred = TabLd8k.pred;
        float pred_code = 36.0f;
        float ener_code = 0.01f;
        for (i = 0; i < l_subfr; i++) {
            ener_code += code[i] * code[i];
        }
        pred_code -= 10.0f * ((float) Math.log10((double) (ener_code / ((float) l_subfr))));
        for (i = 0; i < 4; i++) {
            pred_code += pred[i] * past_qua_en[i];
        }
        return (float) Math.pow(10.0d, ((double) pred_code) / 20.0d);
    }

    static void gain_update(float[] past_qua_en, float g_code) {
        for (int i = 3; i > 0; i--) {
            past_qua_en[i] = past_qua_en[i - 1];
        }
        past_qua_en[0] = 20.0f * ((float) Math.log10((double) g_code));
    }

    static void gain_update_erasure(float[] past_qua_en) {
        int i;
        float av_pred_en = 0.0f;
        for (i = 0; i < 4; i++) {
            av_pred_en += past_qua_en[i];
        }
        av_pred_en = (0.25f * av_pred_en) - 4.0f;
        if (av_pred_en < -14.0f) {
            av_pred_en = -14.0f;
        }
        for (i = 3; i > 0; i--) {
            past_qua_en[i] = past_qua_en[i - 1];
        }
        past_qua_en[0] = av_pred_en;
    }
}
