package net.sf.fmj.media.rtp.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class UDPPacketSender implements PacketConsumer {
    private InetAddress address;
    private int port;
    private DatagramSocket sock;
    private int ttl;

    public UDPPacketSender() throws IOException {
        this(new DatagramSocket());
    }

    public UDPPacketSender(DatagramSocket sock) {
        this.sock = sock;
    }

    public UDPPacketSender(InetAddress remoteAddress, int remotePort) throws IOException {
        if (remoteAddress.isMulticastAddress()) {
            this.sock = new MulticastSocket();
        } else {
            this.sock = new DatagramSocket();
        }
        setRemoteAddress(remoteAddress, remotePort);
    }

    public UDPPacketSender(int localPort) throws IOException {
        this(new DatagramSocket(localPort));
    }

    public UDPPacketSender(int localPort, InetAddress localAddress, InetAddress remoteAddress, int remotePort) throws IOException {
        if (remoteAddress.isMulticastAddress()) {
            MulticastSocket sock = new MulticastSocket(localPort);
            if (localAddress != null) {
                sock.setInterface(localAddress);
            }
            this.sock = sock;
        } else if (localAddress != null) {
            try {
                this.sock = new DatagramSocket(localPort, localAddress);
            } catch (SocketException e) {
                System.out.println(e);
                System.out.println("localPort: " + localPort);
                System.out.println("localAddress: " + localAddress);
                throw e;
            }
        } else {
            this.sock = new DatagramSocket(localPort);
        }
        setRemoteAddress(remoteAddress, remotePort);
    }

    public void closeConsumer() {
        if (this.sock != null) {
            this.sock.close();
            this.sock = null;
        }
    }

    public String consumerString() {
        String s = "UDP Datagram Packet Sender on port " + this.sock.getLocalPort();
        if (this.address != null) {
            return s + " sending to address " + this.address + ", port " + this.port + ", ttl" + this.ttl;
        }
        return s;
    }

    public InetAddress getLocalAddress() {
        return this.sock.getLocalAddress();
    }

    public int getLocalPort() {
        return this.sock.getLocalPort();
    }

    public DatagramSocket getSocket() {
        return this.sock;
    }

    public void send(Packet p, InetAddress addr, int port) throws IOException {
        byte[] data = p.data;
        if (p.offset > 0) {
            byte[] data2 = new byte[p.length];
            System.arraycopy(data, p.offset, data2, 0, p.length);
            data = data2;
        }
        this.sock.send(new DatagramPacket(data, p.length, addr, port));
    }

    public void sendTo(Packet p) throws IOException {
        InetAddress addr = null;
        int port = 0;
        if (p instanceof UDPPacket) {
            UDPPacket udpp = (UDPPacket) p;
            addr = udpp.remoteAddress;
            port = udpp.remotePort;
        }
        if (addr == null) {
            throw new IllegalArgumentException("No address set");
        }
        send(p, addr, port);
    }

    public void setRemoteAddress(InetAddress remoteAddress, int remotePort) {
        this.address = remoteAddress;
        this.port = remotePort;
    }

    public void setttl(int ttl) throws IOException {
        this.ttl = ttl;
        if (this.sock instanceof MulticastSocket) {
            ((MulticastSocket) this.sock).setTTL((byte) this.ttl);
        }
    }
}
