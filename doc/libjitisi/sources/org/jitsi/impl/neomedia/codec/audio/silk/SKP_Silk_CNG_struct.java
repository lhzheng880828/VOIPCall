package org.jitsi.impl.neomedia.codec.audio.silk;

import org.jitsi.impl.neomedia.device.DeviceConfiguration;

/* compiled from: Structs */
class SKP_Silk_CNG_struct {
    int[] CNG_exc_buf_Q10 = new int[DeviceConfiguration.DEFAULT_VIDEO_HEIGHT];
    int CNG_smth_Gain_Q16;
    int[] CNG_smth_NLSF_Q15 = new int[16];
    int[] CNG_synth_state = new int[16];
    int fs_kHz;
    int rand_seed;

    SKP_Silk_CNG_struct() {
    }
}
