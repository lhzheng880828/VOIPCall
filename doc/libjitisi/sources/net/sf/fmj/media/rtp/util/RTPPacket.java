package net.sf.fmj.media.rtp.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RTPPacket extends Packet {
    public Packet base;
    public int[] csrc;
    public byte[] extension;
    public boolean extensionPresent;
    public int extensionType;
    public int marker;
    public int payloadType;
    public int payloadlength;
    public int payloadoffset;
    public int seqnum;
    public int ssrc;
    public long timestamp;

    public RTPPacket(Packet p) {
        super(p);
        this.base = p;
    }

    public void assemble(int len, boolean encrypted) {
        this.length = len;
        this.offset = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len);
        DataOutputStream out = new DataOutputStream(baos);
        try {
            out.writeByte(128);
            int mp = this.payloadType;
            if (this.marker == 1) {
                mp = this.payloadType | 128;
            }
            out.writeByte((byte) mp);
            out.writeShort(this.seqnum);
            out.writeInt((int) this.timestamp);
            out.writeInt(this.ssrc);
            out.write(this.base.data, this.payloadoffset, this.payloadlength);
            this.data = baos.toByteArray();
        } catch (IOException e) {
            System.out.println("caught IOException in DOS");
        }
    }

    public int calcLength() {
        return this.payloadlength + 12;
    }

    public Object clone() {
        RTPPacket p = new RTPPacket((Packet) this.base.clone());
        p.extensionPresent = this.extensionPresent;
        p.marker = this.marker;
        p.payloadType = this.payloadType;
        p.seqnum = this.seqnum;
        p.timestamp = this.timestamp;
        p.ssrc = this.ssrc;
        p.csrc = (int[]) this.csrc.clone();
        p.extensionType = this.extensionType;
        p.extension = this.extension;
        p.payloadoffset = this.payloadoffset;
        p.payloadlength = this.payloadlength;
        return p;
    }

    public String toString() {
        String s = "RTP Packet:\n\tPayload Type: " + this.payloadType + "    Marker: " + this.marker + "\n\tSequence Number: " + this.seqnum + "\n\tTimestamp: " + this.timestamp + "\n\tSSRC (Sync Source): " + this.ssrc + "\n\tPayload Length: " + this.payloadlength + "    Payload Offset: " + this.payloadoffset + "\n";
        if (this.csrc.length > 0) {
            s = s + "Contributing sources:  " + this.csrc[0];
            for (int i = 1; i < this.csrc.length; i++) {
                s = s + ", " + this.csrc[i];
            }
            s = s + "\n";
        }
        if (this.extensionPresent) {
            return s + "\tExtension:  type " + this.extensionType + ", length " + this.extension.length + "\n";
        }
        return s;
    }
}
