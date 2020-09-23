package org.jitsi.impl.neomedia;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import org.ice4j.socket.MultiplexingDatagramSocket;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.packetlogging.PacketLoggingService.ProtocolName;
import org.jitsi.service.packetlogging.PacketLoggingService.TransportName;

public class RTPConnectorUDPOutputStream extends RTPConnectorOutputStream {
    private final DatagramSocket socket;

    public RTPConnectorUDPOutputStream(DatagramSocket socket) {
        this.socket = socket;
    }

    /* access modifiers changed from: protected */
    public void sendToTarget(RawPacket packet, InetSocketAddress target) throws IOException {
        this.socket.send(new DatagramPacket(packet.getBuffer(), packet.getOffset(), packet.getLength(), target.getAddress(), target.getPort()));
    }

    /* access modifiers changed from: protected */
    public void doLogPacket(RawPacket packet, InetSocketAddress target) {
        if (!(this.socket instanceof MultiplexingDatagramSocket)) {
            PacketLoggingService packetLogging = LibJitsi.getPacketLoggingService();
            if (packetLogging != null) {
                packetLogging.logPacket(ProtocolName.RTP, this.socket.getLocalAddress().getAddress(), this.socket.getLocalPort(), target.getAddress().getAddress(), target.getPort(), TransportName.UDP, true, packet.getBuffer(), packet.getOffset(), packet.getLength());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isSocketValid() {
        return this.socket != null;
    }
}
