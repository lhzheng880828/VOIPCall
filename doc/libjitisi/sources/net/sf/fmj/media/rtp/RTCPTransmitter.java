package net.sf.fmj.media.rtp;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;
import net.sf.fmj.media.rtp.util.UDPPacketSender;
import org.jitsi.impl.neomedia.portaudio.Pa;

public class RTCPTransmitter {
    SSRCCache cache;
    int sdescounter;
    RTCPRawSender sender;
    SSRCInfo ssrcInfo;
    OverallStats stats;

    public RTCPTransmitter(SSRCCache cache) {
        this.stats = null;
        this.sdescounter = 0;
        this.ssrcInfo = null;
        this.cache = cache;
        this.stats = cache.sm.defaultstats;
    }

    public RTCPTransmitter(SSRCCache cache, int port, String address) throws UnknownHostException, IOException {
        this(cache, new RTCPRawSender(port, address));
    }

    public RTCPTransmitter(SSRCCache cache, int port, String address, UDPPacketSender sender) throws UnknownHostException, IOException {
        this(cache, new RTCPRawSender(port, address, sender));
    }

    public RTCPTransmitter(SSRCCache cache, RTCPRawSender sender) {
        this(cache);
        setSender(sender);
        this.stats = cache.sm.defaultstats;
    }

    public void bye(int ssrc, byte[] reason) {
        if (this.cache.rtcpsent) {
            double delay;
            this.cache.byestate = true;
            Vector repvec = makereports();
            RTCPPacket[] packets = new RTCPPacket[(repvec.size() + 1)];
            repvec.copyInto(packets);
            RTCPBYEPacket byep = new RTCPBYEPacket(new int[]{ssrc}, reason);
            packets[packets.length - 1] = byep;
            RTCPCompoundPacket cp = new RTCPCompoundPacket(packets);
            if (this.cache.aliveCount() > 50) {
                this.cache.reset(byep.length);
                delay = this.cache.calcReportInterval(this.ssrcInfo.sender, false);
            } else {
                delay = Pa.LATENCY_UNSPECIFIED;
            }
            try {
                Thread.sleep((long) delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            transmit(cp);
            this.sdescounter = 0;
        }
    }

    public void bye(String reason) {
        if (reason != null) {
            bye(this.ssrcInfo.ssrc, reason.getBytes());
        } else {
            bye(this.ssrcInfo.ssrc, null);
        }
    }

    public void close() {
        if (this.sender != null) {
            this.sender.closeConsumer();
        }
    }

    public RTCPRawSender getSender() {
        return this.sender;
    }

    /* access modifiers changed from: protected */
    public RTCPReportBlock[] makerecreports(long time) {
        Vector reports = new Vector();
        Enumeration elements = this.cache.cache.elements();
        while (elements.hasMoreElements()) {
            SSRCInfo info = (SSRCInfo) elements.nextElement();
            if (!info.ours && info.sender) {
                RTCPReportBlock rep = new RTCPReportBlock();
                rep.ssrc = info.ssrc;
                rep.lastseq = (long) (info.maxseq + info.cycles);
                rep.jitter = (int) info.jitter;
                rep.lsr = (long) ((int) ((info.lastSRntptimestamp & 281474976645120L) >> 16));
                rep.dlsr = (long) ((int) (((double) (time - info.lastSRreceiptTime)) * 65.536d));
                rep.packetslost = (int) (((rep.lastseq - ((long) info.baseseq)) + 1) - ((long) info.received));
                if (rep.packetslost < 0) {
                    rep.packetslost = 0;
                }
                double frac = ((double) (rep.packetslost - info.prevlost)) / ((double) (rep.lastseq - ((long) info.prevmaxseq)));
                if (frac < Pa.LATENCY_UNSPECIFIED) {
                    frac = Pa.LATENCY_UNSPECIFIED;
                }
                rep.fractionlost = (int) (256.0d * frac);
                info.prevmaxseq = (int) rep.lastseq;
                info.prevlost = rep.packetslost;
                reports.addElement(rep);
            }
        }
        RTCPReportBlock[] reportsarr = new RTCPReportBlock[reports.size()];
        reports.copyInto(reportsarr);
        return reportsarr;
    }

    /* access modifiers changed from: protected */
    public Vector makereports() {
        Vector packets = new Vector();
        SSRCInfo ourinfo = this.ssrcInfo;
        boolean senderreport = false;
        if (ourinfo.sender) {
            senderreport = true;
        }
        RTCPReportBlock[] reports = makerecreports(System.currentTimeMillis());
        RTCPReportBlock[] firstrep = reports;
        if (reports.length > 31) {
            firstrep = new RTCPReportBlock[31];
            System.arraycopy(reports, 0, firstrep, 0, 31);
        }
        if (senderreport) {
            RTCPSRPacket rTCPSRPacket = new RTCPSRPacket(ourinfo.ssrc, firstrep);
            packets.addElement(rTCPSRPacket);
            long systime = ourinfo.systime == 0 ? System.currentTimeMillis() : ourinfo.systime;
            long secs = systime / 1000;
            rTCPSRPacket.ntptimestamplsw = (long) ((int) (4.294967296E9d * (((double) (systime - (1000 * secs))) / 1000.0d)));
            rTCPSRPacket.ntptimestampmsw = secs;
            rTCPSRPacket.rtptimestamp = (long) ((int) ourinfo.rtptime);
            rTCPSRPacket.packetcount = (long) (ourinfo.maxseq - ourinfo.baseseq);
            rTCPSRPacket.octetcount = (long) ourinfo.bytesreceived;
        } else {
            packets.addElement(new RTCPRRPacket(ourinfo.ssrc, firstrep));
        }
        if (firstrep != reports) {
            for (int offset = 31; offset < reports.length; offset += 31) {
                if (reports.length - offset < 31) {
                    firstrep = new RTCPReportBlock[(reports.length - offset)];
                }
                System.arraycopy(reports, offset, firstrep, 0, firstrep.length);
                packets.addElement(new RTCPRRPacket(ourinfo.ssrc, firstrep));
            }
        }
        RTCPSDESPacket rTCPSDESPacket = new RTCPSDESPacket(new RTCPSDES[1]);
        rTCPSDESPacket.sdes[0] = new RTCPSDES();
        rTCPSDESPacket.sdes[0].ssrc = this.ssrcInfo.ssrc;
        Vector<RTCPSDESItem> itemvec = new Vector();
        itemvec.addElement(new RTCPSDESItem(1, ourinfo.sourceInfo.getCNAME()));
        if (this.sdescounter % 3 == 0) {
            if (!(ourinfo.name == null || ourinfo.name.getDescription() == null)) {
                itemvec.addElement(new RTCPSDESItem(2, ourinfo.name.getDescription()));
            }
            if (!(ourinfo.email == null || ourinfo.email.getDescription() == null)) {
                itemvec.addElement(new RTCPSDESItem(3, ourinfo.email.getDescription()));
            }
            if (!(ourinfo.phone == null || ourinfo.phone.getDescription() == null)) {
                itemvec.addElement(new RTCPSDESItem(4, ourinfo.phone.getDescription()));
            }
            if (!(ourinfo.loc == null || ourinfo.loc.getDescription() == null)) {
                itemvec.addElement(new RTCPSDESItem(5, ourinfo.loc.getDescription()));
            }
            if (!(ourinfo.tool == null || ourinfo.tool.getDescription() == null)) {
                itemvec.addElement(new RTCPSDESItem(6, ourinfo.tool.getDescription()));
            }
            if (!(ourinfo.note == null || ourinfo.note.getDescription() == null)) {
                itemvec.addElement(new RTCPSDESItem(7, ourinfo.note.getDescription()));
            }
        }
        this.sdescounter++;
        rTCPSDESPacket.sdes[0].items = new RTCPSDESItem[itemvec.size()];
        itemvec.copyInto(rTCPSDESPacket.sdes[0].items);
        packets.addElement(rTCPSDESPacket);
        return packets;
    }

    public void report() {
        Vector repvec = makereports();
        RTCPPacket[] packets = new RTCPPacket[repvec.size()];
        repvec.copyInto(packets);
        transmit(new RTCPCompoundPacket(packets));
    }

    public void setSender(RTCPRawSender s) {
        this.sender = s;
    }

    public void setSSRCInfo(SSRCInfo info) {
        this.ssrcInfo = info;
    }

    /* access modifiers changed from: protected */
    public void transmit(RTCPCompoundPacket p) {
        OverallTransStats overallTransStats;
        try {
            this.sender.sendTo(p);
            if (this.ssrcInfo instanceof SendSSRCInfo) {
                RTPTransStats rTPTransStats = ((SendSSRCInfo) this.ssrcInfo).stats;
                rTPTransStats.total_rtcp++;
                overallTransStats = this.cache.sm.transstats;
                overallTransStats.rtcp_sent++;
            }
            this.cache.updateavgrtcpsize(p.length);
            if (this.cache.initial) {
                this.cache.initial = false;
            }
            if (!this.cache.rtcpsent) {
                this.cache.rtcpsent = true;
            }
        } catch (IOException e) {
            this.stats.update(6, 1);
            overallTransStats = this.cache.sm.transstats;
            overallTransStats.transmit_failed++;
        }
    }
}
