package net.sf.fmj.media.rtp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import javax.media.rtp.SessionAddress;
import net.sf.fmj.media.rtp.util.Packet;
import net.sf.fmj.media.rtp.util.PacketFilter;
import net.sf.fmj.media.rtp.util.RTPPacketSender;
import net.sf.fmj.media.rtp.util.UDPPacket;
import net.sf.fmj.media.rtp.util.UDPPacketSender;

public class RTCPRawSender extends PacketFilter {
    private InetAddress destaddr;
    private int destport;

    public RTCPRawSender(int port, String address) throws UnknownHostException, IOException {
        this.destaddr = InetAddress.getByName(address);
        this.destport = port | 1;
        this.destAddressList = null;
    }

    public RTCPRawSender(int port, String address, UDPPacketSender sender) throws UnknownHostException, IOException {
        this(port, address);
        setConsumer(sender);
        this.destAddressList = null;
    }

    public RTCPRawSender(RTPPacketSender sender) {
        setConsumer(sender);
    }

    public void addDestAddr(InetAddress newaddr) {
        if (this.destAddressList == null) {
            this.destAddressList = new Vector();
            this.destAddressList.addElement(this.destaddr);
        }
        int i = 0;
        while (i < this.destAddressList.size() && !((InetAddress) this.destAddressList.elementAt(i)).equals(newaddr)) {
            i++;
        }
        if (i == this.destAddressList.size()) {
            this.destAddressList.addElement(newaddr);
        }
    }

    public void assemble(RTCPCompoundPacket p) {
        p.assemble(p.calcLength(), false);
    }

    public String filtername() {
        return "RTCP Raw Packet Sender";
    }

    public InetAddress getRemoteAddr() {
        return this.destaddr;
    }

    public Packet handlePacket(Packet p) {
        assemble((RTCPCompoundPacket) p);
        if (getConsumer() instanceof RTPPacketSender) {
            return p;
        }
        Packet udpp = new UDPPacket();
        udpp.received = false;
        udpp.data = p.data;
        udpp.offset = p.offset;
        udpp.length = p.length;
        udpp.remoteAddress = this.destaddr;
        udpp.remotePort = this.destport;
        return udpp;
    }

    public Packet handlePacket(Packet p, int index) {
        assemble((RTCPCompoundPacket) p);
        UDPPacket udpp = new UDPPacket();
        udpp.received = false;
        udpp.data = p.data;
        udpp.offset = p.offset;
        udpp.length = p.length;
        udpp.remoteAddress = (InetAddress) this.destAddressList.elementAt(index);
        udpp.remotePort = this.destport;
        return udpp;
    }

    public Packet handlePacket(Packet p, SessionAddress sessionAddress) {
        assemble((RTCPCompoundPacket) p);
        if (getConsumer() instanceof RTPPacketSender) {
            return p;
        }
        Packet udpp = new UDPPacket();
        udpp.received = false;
        udpp.data = p.data;
        udpp.offset = p.offset;
        udpp.length = p.length;
        udpp.remoteAddress = sessionAddress.getControlAddress();
        udpp.remotePort = sessionAddress.getControlPort();
        return udpp;
    }

    public void setDestAddresses(Vector destAddresses) {
        this.destAddressList = destAddresses;
    }
}
