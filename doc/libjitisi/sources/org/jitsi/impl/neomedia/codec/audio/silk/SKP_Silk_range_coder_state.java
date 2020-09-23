package org.jitsi.impl.neomedia.codec.audio.silk;

/* compiled from: Structs */
class SKP_Silk_range_coder_state {
    long base_Q32;
    byte[] buffer = new byte[1024];
    int bufferIx;
    int bufferLength;
    int error;
    long range_Q16;

    SKP_Silk_range_coder_state() {
    }
}
