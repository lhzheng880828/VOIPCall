package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: StructsFLP */
class SKP_Silk_shape_state_FLP {
    float HarmBoost_smth;
    float HarmShapeGain_smth;
    int LastGainIndex;
    float Tilt_smth;

    SKP_Silk_shape_state_FLP() {
    }

    public void memZero() {
        this.LastGainIndex = 0;
        this.HarmBoost_smth = 0.0f;
        this.HarmShapeGain_smth = 0.0f;
        this.Tilt_smth = 0.0f;
    }
}
