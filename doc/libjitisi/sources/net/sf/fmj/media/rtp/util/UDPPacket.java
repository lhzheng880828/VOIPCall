package net.sf.fmj.media.rtp.util;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Date;

public class UDPPacket extends Packet {
    public DatagramPacket datagrampacket;
    public int localPort;
    public InetAddress remoteAddress;
    public int remotePort;

    public String toString() {
        String s = "UDP Packet of size " + this.length;
        if (this.received) {
            return s + " received at " + new Date(this.receiptTime) + " on port " + this.localPort + " from " + this.remoteAddress + " port " + this.remotePort;
        }
        return s;
    }
}
