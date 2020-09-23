package org.jitsi.impl.neomedia.codec.audio.silk;

import java.lang.reflect.Array;

/* compiled from: Structs */
class SKP_Silk_decoder_control {
    int[] Gains_Q16 = new int[4];
    short[] LTPCoef_Q14 = new short[20];
    int LTP_scale_Q14;
    int NLSFInterpCoef_Q2;
    int PERIndex;
    short[][] PredCoef_Q12 = ((short[][]) Array.newInstance(Short.TYPE, new int[]{2, 16}));
    int QuantOffsetType;
    int RateLevelIndex;
    int Seed;
    int[] dummy_int32PredCoef_Q12 = new int[2];
    int[] pitchL = new int[4];
    int sigtype;

    SKP_Silk_decoder_control() {
    }
}
