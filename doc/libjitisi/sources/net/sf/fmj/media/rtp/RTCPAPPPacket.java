package net.sf.fmj.media.rtp;

import com.lti.utils.UnsignedUtils;
import java.io.DataOutputStream;
import java.io.IOException;

public class RTCPAPPPacket extends RTCPPacket {
    byte[] data;
    int name;
    int ssrc;
    int subtype;

    public RTCPAPPPacket(int ssrc, int name, int subtype, byte[] data) {
        this.ssrc = ssrc;
        this.name = name;
        this.subtype = subtype;
        this.data = data;
        this.type = RTCPPacket.APP;
        this.received = false;
        if ((data.length & 3) != 0) {
            throw new IllegalArgumentException("Bad data length");
        } else if (subtype < 0 || subtype > 31) {
            throw new IllegalArgumentException("Bad subtype");
        }
    }

    public RTCPAPPPacket(RTCPPacket parent) {
        super(parent);
        this.type = RTCPPacket.APP;
    }

    /* access modifiers changed from: 0000 */
    public void assemble(DataOutputStream out) throws IOException {
        out.writeByte(this.subtype + 128);
        out.writeByte(RTCPPacket.APP);
        out.writeShort((this.data.length >> 2) + 2);
        out.writeInt(this.ssrc);
        out.writeInt(this.name);
        out.write(this.data);
    }

    public int calcLength() {
        return this.data.length + 12;
    }

    public String nameString(int name) {
        return "" + ((char) (name >>> 24)) + ((char) ((name >>> 16) & UnsignedUtils.MAX_UBYTE)) + ((char) ((name >>> 8) & UnsignedUtils.MAX_UBYTE)) + ((char) (name & UnsignedUtils.MAX_UBYTE));
    }

    public String toString() {
        return "\tRTCP APP Packet from SSRC " + this.ssrc + " with name " + nameString(this.name) + " and subtype " + this.subtype + "\n\tData (length " + this.data.length + "): " + new String(this.data) + "\n";
    }
}
