package net.sf.fmj.media.rtp;

import java.io.DataOutputStream;
import java.io.IOException;
import net.sf.fmj.media.rtp.util.Packet;

public abstract class RTCPPacket extends Packet {
    public static final int APP = 204;
    public static final int BYE = 203;
    public static final int COMPOUND = -1;
    public static final int RR = 201;
    public static final int SDES = 202;
    public static final int SR = 200;
    public Packet base;
    public int type;

    public abstract void assemble(DataOutputStream dataOutputStream) throws IOException;

    public abstract int calcLength();

    public RTCPPacket(Packet p) {
        super(p);
        this.base = p;
    }

    public RTCPPacket(RTCPPacket parent) {
        super(parent);
        this.base = parent.base;
    }
}
