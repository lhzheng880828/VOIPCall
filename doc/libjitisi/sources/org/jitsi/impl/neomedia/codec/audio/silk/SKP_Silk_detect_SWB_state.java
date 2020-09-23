package org.jitsi.impl.neomedia.codec.audio.silk;

import java.lang.reflect.Array;

/* compiled from: Structs */
class SKP_Silk_detect_SWB_state {
    int ActiveSpeech_ms;
    int ConsecSmplsAboveThres;
    int SWB_detected;
    int[][] S_HP_8_kHz = ((int[][]) Array.newInstance(Integer.TYPE, new int[]{3, 2}));
    int WB_detected;

    SKP_Silk_detect_SWB_state() {
    }
}
