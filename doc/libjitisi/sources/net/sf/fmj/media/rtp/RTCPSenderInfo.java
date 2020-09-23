package net.sf.fmj.media.rtp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class RTCPSenderInfo {
    private static final long MSB_0_BASE_TIME = 2085978496000L;
    public static final long MSB_1_BASE_TIME = -2208988800000L;
    public static final int SIZE = 20;
    private long ntpTimestampLSW = 0;
    private long ntpTimestampMSW = 0;
    private long octetCount = 0;
    private long packetCount = 0;
    private long rtpTimestamp = 0;

    public RTCPSenderInfo(byte[] rtcpPacket, int offset, int length) throws IOException {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(rtcpPacket, offset, length));
        this.ntpTimestampMSW = ((long) stream.readInt()) & 4294967295L;
        this.ntpTimestampLSW = ((long) stream.readInt()) & 4294967295L;
        this.rtpTimestamp = ((long) stream.readInt()) & 4294967295L;
        this.packetCount = ((long) stream.readInt()) & 4294967295L;
        this.octetCount = ((long) stream.readInt()) & 4294967295L;
    }

    public long getNtpTimestampLSW() {
        return this.ntpTimestampLSW;
    }

    public long getNtpTimestampMSW() {
        return this.ntpTimestampMSW;
    }

    public double getNtpTimestampSecs() {
        return ((double) getTimestamp()) / 1000.0d;
    }

    public long getOctetCount() {
        return this.octetCount;
    }

    public long getPacketCount() {
        return this.packetCount;
    }

    public long getRtpTimestamp() {
        return this.rtpTimestamp;
    }

    public long getTimestamp() {
        long seconds = this.ntpTimestampMSW;
        long fraction = Math.round((1000.0d * ((double) this.ntpTimestampLSW)) / 4.294967296E9d);
        if ((seconds & 2147483648L) == 0) {
            return (MSB_0_BASE_TIME + (seconds * 1000)) + fraction;
        }
        return (MSB_1_BASE_TIME + (seconds * 1000)) + fraction;
    }

    public String toString() {
        return (((("" + "ntp_ts=" + getNtpTimestampMSW()) + " " + getNtpTimestampLSW()) + " rtp_ts=" + getRtpTimestamp()) + " packet_ct=" + getPacketCount()) + " octect_ct=" + getOctetCount();
    }
}
