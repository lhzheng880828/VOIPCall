package org.jitsi.impl.neomedia.codec.audio.silk;

public class DecodePitch {
    static void SKP_Silk_decode_pitch(int lagIndex, int contourIndex, int[] pitch_lags, int Fs_kHz) {
        int lag = Macros.SKP_SMULBB(2, Fs_kHz) + lagIndex;
        int i;
        if (Fs_kHz == 8) {
            for (i = 0; i < 4; i++) {
                pitch_lags[i] = PitchEstTables.SKP_Silk_CB_lags_stage2[i][contourIndex] + lag;
            }
            return;
        }
        for (i = 0; i < 4; i++) {
            pitch_lags[i] = PitchEstTables.SKP_Silk_CB_lags_stage3[i][contourIndex] + lag;
        }
    }
}
