package net.sf.fmj.media.rtp;

import java.io.DataOutputStream;
import java.io.IOException;

public class RTCPRRPacket extends RTCPPacket {
    RTCPReportBlock[] reports;
    int ssrc;

    RTCPRRPacket(int ssrc, RTCPReportBlock[] reports) {
        this.ssrc = ssrc;
        this.reports = reports;
        if (reports.length > 31) {
            throw new IllegalArgumentException("Too many reports");
        }
    }

    RTCPRRPacket(RTCPPacket parent) {
        super(parent);
        this.type = RTCPPacket.RR;
    }

    /* access modifiers changed from: 0000 */
    public void assemble(DataOutputStream out) throws IOException {
        out.writeByte(this.reports.length + 128);
        out.writeByte(RTCPPacket.RR);
        out.writeShort((this.reports.length * 6) + 1);
        out.writeInt(this.ssrc);
        for (int i = 0; i < this.reports.length; i++) {
            out.writeInt(this.reports[i].ssrc);
            out.writeInt((this.reports[i].packetslost & 16777215) + (this.reports[i].fractionlost << 24));
            out.writeInt((int) this.reports[i].lastseq);
            out.writeInt(this.reports[i].jitter);
            out.writeInt((int) this.reports[i].lsr);
            out.writeInt((int) this.reports[i].dlsr);
        }
    }

    public int calcLength() {
        return (this.reports.length * 24) + 8;
    }

    public String toString() {
        return "\tRTCP RR (receiver report) packet for sync source " + this.ssrc + ":\n" + RTCPReportBlock.toString(this.reports);
    }
}
