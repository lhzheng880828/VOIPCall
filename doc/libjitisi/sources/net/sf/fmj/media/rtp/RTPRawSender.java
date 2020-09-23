package net.sf.fmj.media.rtp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.SessionAddress;
import net.sf.fmj.media.Log;
import net.sf.fmj.media.rtp.util.Packet;
import net.sf.fmj.media.rtp.util.PacketFilter;
import net.sf.fmj.media.rtp.util.RTPPacket;
import net.sf.fmj.media.rtp.util.RTPPacketSender;
import net.sf.fmj.media.rtp.util.UDPPacket;
import net.sf.fmj.media.rtp.util.UDPPacketSender;

public class RTPRawSender extends PacketFilter {
    private InetAddress destaddr;
    private int destport;
    private RTPConnector rtpConnector;
    private DatagramSocket socket;

    public RTPRawSender(int port, String address) throws UnknownHostException, IOException {
        this.socket = null;
        this.rtpConnector = null;
        this.destaddr = InetAddress.getByName(address);
        this.destport = port;
        this.destAddressList = null;
    }

    public RTPRawSender(int port, String address, UDPPacketSender sender) throws UnknownHostException, IOException {
        this(port, address);
        this.socket = sender.getSocket();
        setConsumer(sender);
        this.destAddressList = null;
    }

    public RTPRawSender(RTPPacketSender sender) {
        this.socket = null;
        this.rtpConnector = null;
        this.rtpConnector = sender.getConnector();
        setConsumer(sender);
    }

    public void assemble(RTPPacket p) {
        p.assemble(p.calcLength(), false);
    }

    public String filtername() {
        return "RTP Raw Packet Sender";
    }

    public InetAddress getRemoteAddr() {
        return this.destaddr;
    }

    public int getSendBufSize() {
        try {
            if (this.socket != null) {
                return ((Integer) this.socket.getClass().getMethod("getSendBufferSize", new Class[0]).invoke(this.socket, new Object[0])).intValue();
            }
            if (this.rtpConnector != null) {
                return this.rtpConnector.getSendBufferSize();
            }
            return -1;
        } catch (Exception e) {
        }
    }

    public Packet handlePacket(Packet p) {
        assemble((RTPPacket) p);
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

    public Packet handlePacket(Packet p, int i) {
        return null;
    }

    public Packet handlePacket(Packet p, SessionAddress sessionAddress) {
        assemble((RTPPacket) p);
        if (getConsumer() instanceof RTPPacketSender) {
            return p;
        }
        Packet udpp = new UDPPacket();
        udpp.received = false;
        udpp.data = p.data;
        udpp.offset = p.offset;
        udpp.length = p.length;
        udpp.remoteAddress = sessionAddress.getDataAddress();
        udpp.remotePort = sessionAddress.getDataPort();
        return udpp;
    }

    public void setDestAddresses(Vector destAddresses) {
        this.destAddressList = destAddresses;
    }

    public void setSendBufSize(int size) {
        try {
            if (this.socket != null) {
                this.socket.getClass().getMethod("setSendBufferSize", new Class[]{Integer.TYPE}).invoke(this.socket, new Object[]{new Integer(size)});
            } else if (this.rtpConnector != null) {
                this.rtpConnector.setSendBufferSize(size);
            }
        } catch (Exception e) {
            Log.comment("Cannot set send buffer size: " + e);
        }
    }
}
