package net.sf.fmj.media.rtp.util;

import java.util.Date;

public class Packet {
    public byte[] data;
    public int length;
    public int offset;
    public long receiptTime;
    public boolean received = true;

    public Packet(Packet p) {
        this.data = p.data;
        this.offset = p.offset;
        this.length = p.length;
        this.received = p.received;
        this.receiptTime = p.receiptTime;
    }

    public Object clone() {
        Packet p = new Packet(this);
        p.data = (byte[]) this.data.clone();
        return p;
    }

    public String toString() {
        String s = "Packet of size " + this.length;
        if (this.received) {
            return s + " received at " + new Date(this.receiptTime);
        }
        return s;
    }
}
