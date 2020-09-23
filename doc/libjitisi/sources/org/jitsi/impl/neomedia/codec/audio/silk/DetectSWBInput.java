package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.device.DeviceConfiguration;

public class DetectSWBInput {
    static void SKP_Silk_detect_SWB_input(SKP_Silk_detect_SWB_state psSWBdetect, short[] samplesIn, int samplesIn_offset, int nSamplesIn) {
        int[] shift = new int[1];
        short[] in_HP_8_kHz = new short[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
        int[] energy_32 = new int[1];
        int HP_8_kHz_len = Math.max(Math.min(nSamplesIn, DeviceConfiguration.DEFAULT_VIDEO_HEIGHT), 0);
        Biquad.SKP_Silk_biquad(samplesIn, samplesIn_offset, TablesOther.SKP_Silk_SWB_detect_B_HP_Q13[0], TablesOther.SKP_Silk_SWB_detect_A_HP_Q13[0], psSWBdetect.S_HP_8_kHz[0], in_HP_8_kHz, 0, HP_8_kHz_len);
        for (int i = 1; i < 3; i++) {
            Biquad.SKP_Silk_biquad(in_HP_8_kHz, 0, TablesOther.SKP_Silk_SWB_detect_B_HP_Q13[i], TablesOther.SKP_Silk_SWB_detect_A_HP_Q13[i], psSWBdetect.S_HP_8_kHz[i], in_HP_8_kHz, 0, HP_8_kHz_len);
        }
        SumSqrShift.SKP_Silk_sum_sqr_shift(energy_32, shift, in_HP_8_kHz, 0, HP_8_kHz_len);
        if (energy_32[0] > (Macros.SKP_SMULBB(10, HP_8_kHz_len) >> shift[0])) {
            psSWBdetect.ConsecSmplsAboveThres += nSamplesIn;
            if (psSWBdetect.ConsecSmplsAboveThres > 7200) {
                psSWBdetect.SWB_detected = 1;
            }
        } else {
            psSWBdetect.ConsecSmplsAboveThres -= nSamplesIn;
            psSWBdetect.ConsecSmplsAboveThres = Math.max(psSWBdetect.ConsecSmplsAboveThres, 0);
        }
        if (psSWBdetect.ActiveSpeech_ms > 15000 && psSWBdetect.SWB_detected == 0) {
            psSWBdetect.WB_detected = 1;
        }
    }
}
