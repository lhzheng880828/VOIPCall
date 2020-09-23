package net.sf.fmj.media.rtp;

import java.io.DataOutputStream;
import java.io.IOException;

public class RTCPBYEPacket extends RTCPPacket {
    byte[] reason;
    int[] ssrc;

    public RTCPBYEPacket(int[] ssrc, byte[] reason) {
        this.ssrc = ssrc;
        if (reason != null) {
            this.reason = reason;
        } else {
            this.reason = new byte[0];
        }
        if (ssrc.length > 31) {
            throw new IllegalArgumentException("Too many SSRCs");
        }
    }

    public RTCPBYEPacket(RTCPPacket parent) {
        super(parent);
        this.type = RTCPPacket.BYE;
    }

    /* access modifiers changed from: 0000 */
    public void assemble(DataOutputStream out) throws IOException {
        int i;
        out.writeByte(this.ssrc.length + 128);
        out.writeByte(RTCPPacket.BYE);
        out.writeShort((this.reason.length <= 0 ? 0 : (this.reason.length + 4) >> 2) + this.ssrc.length);
        for (int writeInt : this.ssrc) {
            out.writeInt(writeInt);
        }
        if (this.reason.length > 0) {
            out.writeByte(this.reason.length);
            out.write(this.reason);
            for (i = (((this.reason.length + 4) & -4) - this.reason.length) - 1; i > 0; i--) {
                out.writeByte(0);
            }
        }
    }

    public int calcLength() {
        return (this.reason.length <= 0 ? 0 : (this.reason.length + 4) & -4) + ((this.ssrc.length << 2) + 4);
    }

    public String toString() {
        return "\tRTCP BYE packet for sync source(s) " + toString(this.ssrc) + " for " + (this.reason.length <= 0 ? "no reason" : "reason " + new String(this.reason)) + "\n";
    }

    public String toString(int[] ints) {
        if (ints.length == 0) {
            return "(none)";
        }
        String s = "" + ints[0];
        for (int i = 1; i < ints.length; i++) {
            s = s + ", " + ints[i];
        }
        return s;
    }
}
