package net.sf.fmj.media.rtp.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPPacketReceiver implements PacketSource {
    byte[] dataBuf;
    private int maxsize;
    private DatagramSocket sock;

    public UDPPacketReceiver(DatagramSocket sock, int maxsize) {
        this.dataBuf = new byte[1];
        this.sock = sock;
        this.maxsize = maxsize;
        try {
            sock.setSoTimeout(5000);
        } catch (SocketException e) {
            System.out.println("could not set timeout on socket");
        }
    }

    public UDPPacketReceiver(int localPort, String localAddress, int remotePort, String remoteAddress, int maxsize, DatagramSocket localSocket) throws SocketException, UnknownHostException, IOException {
        this.dataBuf = new byte[1];
        InetAddress localInetAddr = InetAddress.getByName(localAddress);
        InetAddress remoteInetAddr = InetAddress.getByName(remoteAddress);
        if (remoteInetAddr.isMulticastAddress()) {
            MulticastSocket sock = new MulticastSocket(remotePort);
            sock.joinGroup(remoteInetAddr);
            this.sock = sock;
            this.maxsize = maxsize;
        } else {
            if (localSocket != null) {
                this.sock = localSocket;
            } else {
                this.sock = new DatagramSocket(localPort, localInetAddr);
            }
            if (remoteAddress == null) {
            }
            this.maxsize = maxsize;
        }
        try {
            this.sock.setSoTimeout(5000);
        } catch (SocketException e) {
            System.out.println("could not set timeout on socket");
        }
    }

    public void closeSource() {
        if (this.sock != null) {
            this.sock.close();
            this.sock = null;
        }
    }

    public DatagramSocket getSocket() {
        return this.sock;
    }

    public Packet receiveFrom() throws IOException {
        int len;
        DatagramPacket dp;
        do {
            if (this.dataBuf.length < this.maxsize) {
                this.dataBuf = new byte[this.maxsize];
            }
            dp = new DatagramPacket(this.dataBuf, this.maxsize);
            this.sock.receive(dp);
            len = dp.getLength();
            if (len > (this.maxsize >> 1)) {
                this.maxsize = len << 1;
            }
        } while (len >= dp.getData().length);
        UDPPacket p = new UDPPacket();
        p.receiptTime = System.currentTimeMillis();
        p.data = dp.getData();
        p.offset = 0;
        p.length = len;
        p.datagrampacket = dp;
        p.localPort = this.sock.getLocalPort();
        p.remotePort = dp.getPort();
        p.remoteAddress = dp.getAddress();
        return p;
    }

    public String sourceString() {
        return "UDP Datagram Packet Receiver on port " + this.sock.getLocalPort() + "on local address " + this.sock.getLocalAddress();
    }
}
