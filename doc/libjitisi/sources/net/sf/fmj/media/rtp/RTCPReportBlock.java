package net.sf.fmj.media.rtp;

import javax.media.rtp.rtcp.Feedback;
import net.sf.fmj.media.rtp.util.Signed;

public class RTCPReportBlock implements Feedback {
    long dlsr;
    int fractionlost;
    int jitter;
    long lastseq;
    long lsr;
    int packetslost;
    long receiptTime;
    int ssrc;

    public static String toString(RTCPReportBlock[] reports) {
        String s = "";
        for (Object obj : reports) {
            s = s + obj;
        }
        return s;
    }

    public long getDLSR() {
        return this.dlsr;
    }

    public int getFractionLost() {
        return this.fractionlost;
    }

    public long getJitter() {
        return (long) this.jitter;
    }

    public long getLSR() {
        return this.lsr;
    }

    public long getNumLost() {
        return (long) this.packetslost;
    }

    public long getSSRC() {
        return (long) this.ssrc;
    }

    public long getXtndSeqNum() {
        return this.lastseq;
    }

    public String toString() {
        long printssrc = (long) this.ssrc;
        if (this.ssrc < 0) {
            printssrc = Signed.UnsignedInt(this.ssrc);
        }
        return "\t\tFor source " + printssrc + "\n\t\t\tFraction of packets lost: " + this.fractionlost + " (" + (((double) this.fractionlost) / 256.0d) + ")" + "\n\t\t\tPackets lost: " + this.packetslost + "\n\t\t\tLast sequence number: " + this.lastseq + "\n\t\t\tJitter: " + this.jitter + "\n\t\t\tLast SR packet received at time " + this.lsr + "\n\t\t\tDelay since last SR packet received: " + this.dlsr + " (" + (((double) this.dlsr) / 65536.0d) + " seconds)\n";
    }
}
