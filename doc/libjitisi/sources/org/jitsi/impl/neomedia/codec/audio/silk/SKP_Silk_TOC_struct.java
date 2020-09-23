package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: SDKAPI */
class SKP_Silk_TOC_struct {
    int corrupt;
    int framesInPacket;
    int fs_kHz;
    int inbandLBRR;
    int[] sigtypeFlags = new int[5];
    int[] vadFlags = new int[5];

    SKP_Silk_TOC_struct() {
    }
}
