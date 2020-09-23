package org.jitsi.impl.neomedia.codec.audio.ulaw;

public class Packetizer extends com.sun.media.codec.audio.ulaw.Packetizer {
    public Packetizer() {
        this.packetSize = 160;
        setPacketSize(this.packetSize);
        this.PLUGIN_NAME = "ULaw Packetizer";
    }
}
