package com.sun.media.controls;

import javax.media.Codec;
import javax.media.control.PacketSizeControl;
import org.jitsi.android.util.java.awt.Component;

public class PacketSizeAdapter implements PacketSizeControl {
    protected Codec owner;
    protected int packetSize;
    protected boolean settable;

    public PacketSizeAdapter(Codec owner, int size, boolean settable) {
        this.owner = owner;
        this.packetSize = size;
        this.settable = settable;
    }

    public Component getControlComponent() {
        throw new UnsupportedOperationException();
    }

    public int getPacketSize() {
        return this.packetSize;
    }

    public int setPacketSize(int numBytes) {
        throw new UnsupportedOperationException();
    }
}
