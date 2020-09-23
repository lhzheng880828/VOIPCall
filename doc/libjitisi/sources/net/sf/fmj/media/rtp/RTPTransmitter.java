package net.sf.fmj.media.rtp;

import java.io.IOException;
import java.net.UnknownHostException;
import javax.media.Buffer;
import net.sf.fmj.media.rtp.util.Packet;
import net.sf.fmj.media.rtp.util.RTPPacket;
import net.sf.fmj.media.rtp.util.UDPPacketSender;

public class RTPTransmitter {
    SSRCCache cache;
    RTPRawSender sender;

    public RTPTransmitter(SSRCCache cache) {
        this.cache = cache;
    }

    public RTPTransmitter(SSRCCache cache, int port, String address) throws UnknownHostException, IOException {
        this(cache, new RTPRawSender(port, address));
    }

    public RTPTransmitter(SSRCCache cache, int port, String address, UDPPacketSender sender) throws UnknownHostException, IOException {
        this(cache, new RTPRawSender(port, address, sender));
    }

    public RTPTransmitter(SSRCCache cache, RTPRawSender sender) {
        this(cache);
        setSender(sender);
    }

    public void close() {
        if (this.sender != null) {
            this.sender.closeConsumer();
        }
    }

    public RTPRawSender getSender() {
        return this.sender;
    }

    /* access modifiers changed from: protected */
    public RTPPacket MakeRTPPacket(Buffer b, SendSSRCInfo info) {
        byte[] data = (byte[]) b.getData();
        if (data == null) {
            return null;
        }
        Packet p = new Packet();
        p.data = data;
        p.offset = 0;
        p.length = b.getLength();
        p.received = false;
        RTPPacket rtp = new RTPPacket(p);
        if ((b.getFlags() & 2048) != 0) {
            rtp.marker = 1;
        } else {
            rtp.marker = 0;
        }
        info.packetsize += b.getLength();
        rtp.payloadType = info.payloadType;
        rtp.seqnum = (int) info.getSequenceNumber(b);
        rtp.timestamp = info.rtptime;
        rtp.ssrc = info.ssrc;
        rtp.payloadoffset = b.getOffset();
        rtp.payloadlength = b.getLength();
        info.bytesreceived += b.getLength();
        info.maxseq++;
        info.lasttimestamp = rtp.timestamp;
        return rtp;
    }

    public void setSender(RTPRawSender s) {
        this.sender = s;
    }

    /* access modifiers changed from: protected */
    public void transmit(RTPPacket p) {
        try {
            this.sender.sendTo(p);
        } catch (IOException e) {
            OverallTransStats overallTransStats = this.cache.sm.transstats;
            overallTransStats.transmit_failed++;
        }
    }

    public void TransmitPacket(Buffer b, SendSSRCInfo info) {
        info.rtptime = info.getTimeStamp(b);
        if (b.getHeader() instanceof Long) {
            info.systime = ((Long) b.getHeader()).longValue();
        } else {
            info.systime = System.currentTimeMillis();
        }
        RTPPacket p = MakeRTPPacket(b, info);
        if (p != null) {
            transmit(p);
            RTPTransStats rTPTransStats = info.stats;
            rTPTransStats.total_pdu++;
            info.stats.total_bytes += b.getLength();
            OverallTransStats overallTransStats = this.cache.sm.transstats;
            overallTransStats.rtp_sent++;
            this.cache.sm.transstats.bytes_sent += b.getLength();
        }
    }
}
