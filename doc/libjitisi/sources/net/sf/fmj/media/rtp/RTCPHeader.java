package net.sf.fmj.media.rtp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class RTCPHeader {
    private static final int PADDING_MASK = 8192;
    private static final int PADDING_SHIFT = 13;
    private static final int RCOUNT_MASK = 7936;
    private static final int RCOUNT_SHIFT = 8;
    public static final int SDES_CNAME = 1;
    public static final int SDES_EMAIL = 3;
    public static final int SDES_LOC = 5;
    public static final int SDES_NAME = 2;
    public static final int SDES_NOTE = 7;
    public static final int SDES_PHONE = 4;
    public static final int SDES_SKIP = 8;
    public static final int SDES_TOOL = 6;
    public static final int SIZE = 8;
    private static final int TYPE_MASK = 255;
    private static final int TYPE_SHIFT = 0;
    public static final int VERSION = 2;
    private static final int VERSION_MASK = 49152;
    private static final int VERSION_SHIFT = 14;
    private int flags;
    private int length;
    private long ssrc;

    public RTCPHeader(byte[] data, int offset, int length) throws IOException {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data, offset, length));
        this.flags = stream.readUnsignedShort();
        this.length = stream.readUnsignedShort();
        this.ssrc = ((long) stream.readInt()) & 4294967295L;
        if (getVersion() != (short) 2) {
            throw new IOException("Invalid RTCP Version");
        } else if (getLength() > length) {
            throw new IOException("Invalid Length");
        }
    }

    public RTCPHeader(DatagramPacket packet) throws IOException {
        this(packet.getData(), packet.getOffset(), packet.getLength());
    }

    public int getFlags() {
        return this.flags;
    }

    public int getLength() {
        return this.length;
    }

    public short getPacketType() {
        return (short) ((getFlags() & 255) >> 0);
    }

    public short getPadding() {
        return (short) ((getFlags() & 8192) >> 13);
    }

    public short getReceptionCount() {
        return (short) ((getFlags() & RCOUNT_MASK) >> 8);
    }

    public long getSsrc() {
        return this.ssrc;
    }

    public short getVersion() {
        return (short) ((getFlags() & VERSION_MASK) >> 14);
    }

    public void print() {
        System.err.println(getVersion() + "|" + getPadding() + "|" + getReceptionCount() + "|" + getPacketType() + "|" + getLength() + "|" + getSsrc());
    }
}
