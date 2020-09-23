package net.sf.fmj.media.rtp;

import java.io.DataOutputStream;
import java.io.IOException;

public class RTCPSRPacket extends RTCPPacket {
    long ntptimestamplsw;
    long ntptimestampmsw;
    long octetcount;
    long packetcount;
    RTCPReportBlock[] reports;
    long rtptimestamp;
    int ssrc;

    RTCPSRPacket(int ssrc, RTCPReportBlock[] reports) {
        this.ssrc = ssrc;
        this.reports = reports;
        if (reports.length > 31) {
            throw new IllegalArgumentException("Too many reports");
        }
    }

    RTCPSRPacket(RTCPPacket parent) {
        super(parent);
        this.type = 200;
    }

    /* access modifiers changed from: 0000 */
    public void assemble(DataOutputStream out) throws IOException {
        out.writeByte(this.reports.length + 128);
        out.writeByte(200);
        out.writeShort((this.reports.length * 6) + 6);
        out.writeInt(this.ssrc);
        out.writeInt((int) this.ntptimestampmsw);
        out.writeInt((int) this.ntptimestamplsw);
        out.writeInt((int) this.rtptimestamp);
        out.writeInt((int) this.packetcount);
        out.writeInt((int) this.octetcount);
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
        return (this.reports.length * 24) + 28;
    }

    public String toString() {
        return "\tRTCP SR (sender report) packet for sync source " + this.ssrc + "\n\t\tNTP timestampMSW: " + this.ntptimestampmsw + "\n\t\tNTP timestampLSW: " + this.ntptimestamplsw + "\n\t\tRTP timestamp: " + this.rtptimestamp + "\n\t\tnumber of packets sent: " + this.packetcount + "\n\t\tnumber of octets (bytes) sent: " + this.octetcount + "\n" + RTCPReportBlock.toString(this.reports);
    }
}
