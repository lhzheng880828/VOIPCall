package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Structs */
class SKP_Silk_VAD_state {
    int[] AnaState = new int[2];
    int[] AnaState1 = new int[2];
    int[] AnaState2 = new int[2];
    short HPstate;
    int[] NL = new int[4];
    int[] NoiseLevelBias = new int[4];
    int[] NrgRatioSmth_Q8 = new int[4];
    int[] XnrgSubfr = new int[4];
    int counter;
    int[] inv_NL = new int[4];

    SKP_Silk_VAD_state() {
    }
}
