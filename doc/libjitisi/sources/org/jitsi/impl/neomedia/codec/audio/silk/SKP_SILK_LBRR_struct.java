package org.jitsi.impl.neomedia.codec.audio.silk;

import java.util.Arrays;

/* compiled from: Structs */
class SKP_SILK_LBRR_struct {
    int nBytes;
    byte[] payload = new byte[1024];
    int usage;

    SKP_SILK_LBRR_struct() {
    }

    public void memZero() {
        this.nBytes = 0;
        this.usage = 0;
        Arrays.fill(this.payload, (byte) 0);
    }
}
