package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Structs */
class SKP_Silk_encoder_control {
    int[] GainsIndices = new int[4];
    int LBRR_usage;
    int[] LTPIndex = new int[4];
    int LTP_scaleIndex;
    int[] NLSFIndices = new int[10];
    int NLSFInterpCoef_Q2;
    int PERIndex;
    int QuantOffsetType;
    int RateLevelIndex;
    int Seed;
    int contourIndex;
    int lagIndex;
    int[] pitchL = new int[4];
    int sigtype;

    SKP_Silk_encoder_control() {
    }
}
