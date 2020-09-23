package org.jitsi.impl.neomedia.transform.dtmf;

import com.lti.utils.UnsignedUtils;
import org.jitsi.impl.neomedia.RawPacket;
import org.jitsi.util.Logger;

public class DtmfRawPacket extends RawPacket {
    private static final Logger logger = Logger.getLogger(DtmfRawPacket.class);
    private int code;
    private int duration;
    private boolean end;
    private int volume;

    public DtmfRawPacket(byte[] buffer, int offset, int length, byte payload) {
        super(buffer, offset, length);
        setPayload(payload);
    }

    public DtmfRawPacket(RawPacket pkt) {
        super(pkt.getBuffer(), pkt.getOffset(), pkt.getLength());
        int at = getHeaderLength();
        int at2 = at + 1;
        this.code = readByte(at);
        at = at2 + 1;
        byte b = readByte(at2);
        this.end = (b & 128) != 0;
        this.volume = b & 127;
        at2 = at + 1;
        at = at2 + 1;
        this.duration = ((readByte(at) & UnsignedUtils.MAX_UBYTE) << 8) | (readByte(at2) & UnsignedUtils.MAX_UBYTE);
    }

    public void init(int code, boolean end, boolean marker, int duration, long timestamp, int volume) {
        if (logger.isTraceEnabled()) {
            logger.trace("DTMF send on RTP, code : " + code + " duration = " + duration + " timestamps = " + timestamp + " Marker = " + marker + " End = " + end);
        }
        setMarker(marker);
        setTimestamp(timestamp);
        setDtmfPayload(code, end, duration, volume);
    }

    private void setDtmfPayload(int code, boolean end, int duration, int volume) {
        this.code = code;
        this.end = end;
        this.duration = duration;
        this.volume = volume;
        int headerLength = getHeaderLength();
        int i = headerLength + 1;
        writeByte(headerLength, (byte) code);
        headerLength = i + 1;
        writeByte(i, end ? (byte) (volume | 128) : (byte) (volume & 127));
        i = headerLength + 1;
        writeByte(headerLength, (byte) (duration >> 8));
        headerLength = i + 1;
        writeByte(i, (byte) duration);
        setLength(headerLength);
    }

    public int getCode() {
        return this.code;
    }

    public boolean isEnd() {
        return this.end;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getVolume() {
        return this.volume;
    }
}
