package net.sf.fmj.media.rtp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import javax.media.rtp.rtcp.Feedback;

public class RTCPFeedback implements Feedback {
    public static final int SIZE = 24;
    private long dlsr = 0;
    private int fractionLost = 0;
    private long jitter = 0;
    private long lsr = 0;
    private long numLost = 0;
    private long ssrc = 0;
    private long xtndSeqNum = 0;

    public RTCPFeedback(byte[] data, int offset, int length) throws IOException {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(data, offset, length));
        this.ssrc = ((long) stream.readInt()) & 4294967295L;
        this.fractionLost = stream.readUnsignedByte();
        this.numLost = (long) ((stream.readUnsignedShort() << 8) | stream.readUnsignedByte());
        this.xtndSeqNum = ((long) stream.readInt()) & 4294967295L;
        this.jitter = ((long) stream.readInt()) & 4294967295L;
        this.lsr = ((long) stream.readInt()) & 4294967295L;
        this.dlsr = ((long) stream.readInt()) & 4294967295L;
    }

    public long getDLSR() {
        return this.dlsr;
    }

    public int getFractionLost() {
        return this.fractionLost;
    }

    public long getJitter() {
        return this.jitter;
    }

    public long getLSR() {
        return this.lsr;
    }

    public long getNumLost() {
        return this.numLost;
    }

    public long getSSRC() {
        return this.ssrc;
    }

    public long getXtndSeqNum() {
        return this.xtndSeqNum;
    }
}
