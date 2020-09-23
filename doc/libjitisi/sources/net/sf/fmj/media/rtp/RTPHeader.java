package net.sf.fmj.media.rtp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;

public class RTPHeader {
    private static final int CSRC_MASK = 3840;
    private static final int CSRC_SHIFT = 8;
    private static final int EXTENSION_MASK = 4096;
    private static final int EXTENSION_SHIFT = 12;
    private static final int MARKER_MASK = 128;
    private static final int MARKER_SHIFT = 7;
    public static final int MAX_PAYLOAD = 127;
    public static final int MAX_SEQUENCE = 65535;
    private static final int PADDING_MASK = 8192;
    private static final int PADDING_SHIFT = 13;
    public static final int SIZE = 12;
    private static final int TYPE_MASK = 127;
    private static final int TYPE_SHIFT = 0;
    public static final long UINT_TO_LONG_CONVERT = 4294967295L;
    public static final int VERSION = 2;
    private static final int VERSION_MASK = 49152;
    private static final int VERSION_SHIFT = 14;
    private int flags;
    private int sequence;
    private long ssrc;
    private long timestamp;

    public RTPHeader(byte[] data, int offset, int length) throws IOException {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data, offset, length));
        this.flags = stream.readUnsignedShort();
        this.sequence = stream.readUnsignedShort();
        this.timestamp = ((long) stream.readInt()) & 4294967295L;
        this.ssrc = ((long) stream.readInt()) & 4294967295L;
        if (getVersion() != (short) 2) {
            throw new IOException("Invalid Version");
        }
    }

    public RTPHeader(DatagramPacket packet) throws IOException {
        this(packet.getData(), packet.getOffset(), packet.getLength());
    }

    /* access modifiers changed from: 0000 */
    public short getCsrcCount() {
        return (short) ((getFlags() & CSRC_MASK) >> 8);
    }

    /* access modifiers changed from: 0000 */
    public short getExtension() {
        return (short) ((getFlags() & 4096) >> 12);
    }

    public int getFlags() {
        return this.flags;
    }

    /* access modifiers changed from: 0000 */
    public short getMarker() {
        return (short) ((getFlags() & 128) >> 7);
    }

    /* access modifiers changed from: 0000 */
    public short getPacketType() {
        return (short) ((getFlags() & 127) >> 0);
    }

    /* access modifiers changed from: 0000 */
    public short getPadding() {
        return (short) ((getFlags() & 8192) >> 13);
    }

    /* access modifiers changed from: 0000 */
    public int getSequence() {
        return this.sequence;
    }

    /* access modifiers changed from: 0000 */
    public int getSize() {
        return (getCsrcCount() * 4) + 12;
    }

    /* access modifiers changed from: 0000 */
    public long getSsrc() {
        return this.ssrc;
    }

    /* access modifiers changed from: 0000 */
    public long getTimestamp() {
        return this.timestamp;
    }

    /* access modifiers changed from: 0000 */
    public short getVersion() {
        return (short) ((getFlags() & VERSION_MASK) >> 14);
    }

    public void print() {
        System.err.println(getVersion() + "|" + getPadding() + "|" + getExtension() + "|" + getCsrcCount() + "|" + getMarker() + "|" + getPacketType() + "|" + getSequence() + "|" + getTimestamp() + "|" + getSsrc());
    }
}
