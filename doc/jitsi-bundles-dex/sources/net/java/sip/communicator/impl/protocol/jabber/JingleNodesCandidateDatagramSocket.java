package net.java.sip.communicator.impl.protocol.jabber;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import org.ice4j.TransportAddress;
import org.ice4j.socket.DelegatingDatagramSocket;
import org.ice4j.socket.StunDatagramPacketFilter;

public class JingleNodesCandidateDatagramSocket extends DatagramSocket {
    private JingleNodesCandidate jingleNodesCandidate;
    private long lastLostPacketLogTime = 0;
    private long lastRtpSequenceNumber = -1;
    private TransportAddress localEndPoint = null;
    private long nbLostRtpPackets = 0;
    private long nbReceivedRtpPackets = 0;
    private long nbSentRtpPackets = 0;

    public JingleNodesCandidateDatagramSocket(JingleNodesCandidate jingleNodesCandidate, TransportAddress localEndPoint) throws SocketException {
        super((SocketAddress) null);
        this.jingleNodesCandidate = jingleNodesCandidate;
        this.localEndPoint = localEndPoint;
    }

    public void send(DatagramPacket p) throws IOException {
        DatagramPacket packet = new DatagramPacket(p.getData(), p.getOffset(), p.getLength(), new InetSocketAddress(this.localEndPoint.getAddress(), this.localEndPoint.getPort()));
        super.send(packet);
        this.nbSentRtpPackets++;
        DelegatingDatagramSocket.logPacketToPcap(packet, this.nbSentRtpPackets, true, super.getLocalAddress(), super.getLocalPort());
    }

    public void receive(DatagramPacket p) throws IOException {
        super.receive(p);
        this.nbReceivedRtpPackets++;
        DelegatingDatagramSocket.logPacketToPcap(p, this.nbReceivedRtpPackets, false, super.getLocalAddress(), super.getLocalPort());
        updateRtpLosses(p);
    }

    public InetAddress getLocalAddress() {
        return getLocalSocketAddress().getAddress();
    }

    public int getLocalPort() {
        return getLocalSocketAddress().getPort();
    }

    public InetSocketAddress getLocalSocketAddress() {
        return this.jingleNodesCandidate.getTransportAddress();
    }

    public void updateRtpLosses(DatagramPacket p) {
        if (!StunDatagramPacketFilter.isStunPacket(p)) {
            long newSeq = DelegatingDatagramSocket.getRtpSequenceNumber(p);
            if (this.lastRtpSequenceNumber != -1) {
                this.nbLostRtpPackets += DelegatingDatagramSocket.getNbLost(this.lastRtpSequenceNumber, newSeq);
            }
            this.lastRtpSequenceNumber = newSeq;
            this.lastLostPacketLogTime = DelegatingDatagramSocket.logRtpLosses(this.nbLostRtpPackets, this.nbReceivedRtpPackets, this.lastLostPacketLogTime);
        }
    }
}
