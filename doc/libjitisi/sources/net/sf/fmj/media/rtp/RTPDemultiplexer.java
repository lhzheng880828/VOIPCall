package net.sf.fmj.media.rtp;

import javax.media.Buffer;
import net.sf.fmj.media.rtp.util.RTPPacket;

public class RTPDemultiplexer {
    private Buffer buffer = new Buffer();
    private SSRCCache cache;
    private RTPRawReceiver rtpr;
    private StreamSynch streamSynch;

    public RTPDemultiplexer(SSRCCache c, RTPRawReceiver r, StreamSynch streamSynch) {
        this.cache = c;
        this.rtpr = r;
        this.streamSynch = streamSynch;
    }

    public String consumerString() {
        return "RTP DeMultiplexer";
    }

    public void demuxpayload(SourceRTPPacket sp) {
        SSRCInfo info = sp.ssrcinfo;
        RTPPacket rtpPacket = sp.p;
        info.payloadType = rtpPacket.payloadType;
        if (info.dstream != null) {
            this.buffer.setData(rtpPacket.base.data);
            this.buffer.setFlags(0);
            if (rtpPacket.marker == 1) {
                this.buffer.setFlags(this.buffer.getFlags() | 2048);
            }
            this.buffer.setLength(rtpPacket.payloadlength);
            this.buffer.setOffset(rtpPacket.payloadoffset);
            this.buffer.setTimeStamp(this.streamSynch.calcTimestamp(info.ssrc, rtpPacket.payloadType, rtpPacket.timestamp));
            this.buffer.setFlags(this.buffer.getFlags() | 4096);
            this.buffer.setSequenceNumber((long) rtpPacket.seqnum);
            this.buffer.setFormat(info.dstream.getFormat());
            info.dstream.add(this.buffer, info.wrapped, this.rtpr);
        }
    }
}
