package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Structs */
class SKP_Silk_PLC_struct {
    short[] LTPCoef_Q14 = new short[5];
    int conc_energy;
    int conc_energy_shift;
    int fs_kHz;
    int last_frame_lost;
    int pitchL_Q8;
    int[] prevGain_Q16 = new int[4];
    short[] prevLPC_Q12 = new short[16];
    short prevLTP_scale_Q14;
    short randScale_Q14;
    int rand_seed;

    SKP_Silk_PLC_struct() {
    }
}
