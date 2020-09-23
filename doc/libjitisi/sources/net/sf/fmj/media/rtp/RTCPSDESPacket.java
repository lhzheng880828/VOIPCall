package net.sf.fmj.media.rtp;

import java.io.DataOutputStream;
import java.io.IOException;

public class RTCPSDESPacket extends RTCPPacket {
    public RTCPSDES[] sdes;

    public RTCPSDESPacket(RTCPPacket parent) {
        super(parent);
        this.type = RTCPPacket.SDES;
    }

    public RTCPSDESPacket(RTCPSDES[] sdes) {
        this.sdes = sdes;
        if (sdes.length > 31) {
            throw new IllegalArgumentException("Too many SDESs");
        }
    }

    /* access modifiers changed from: 0000 */
    public void assemble(DataOutputStream out) throws IOException {
        out.writeByte(this.sdes.length + 128);
        out.writeByte(RTCPPacket.SDES);
        out.writeShort((calcLength() - 4) >> 2);
        for (int i = 0; i < this.sdes.length; i++) {
            int j;
            out.writeInt(this.sdes[i].ssrc);
            int sublen = 0;
            for (j = 0; j < this.sdes[i].items.length; j++) {
                out.writeByte(this.sdes[i].items[j].type);
                out.writeByte(this.sdes[i].items[j].data.length);
                out.write(this.sdes[i].items[j].data);
                sublen += this.sdes[i].items[j].data.length + 2;
            }
            for (j = ((sublen + 4) & -4) - sublen; j > 0; j--) {
                out.writeByte(0);
            }
        }
    }

    public int calcLength() {
        int len = 4;
        for (int i = 0; i < this.sdes.length; i++) {
            int sublen = 5;
            for (RTCPSDESItem rTCPSDESItem : this.sdes[i].items) {
                sublen += rTCPSDESItem.data.length + 2;
            }
            len += (sublen + 3) & -4;
        }
        return len;
    }

    public String toString() {
        return "\tRTCP SDES Packet:\n" + RTCPSDES.toString(this.sdes);
    }
}
