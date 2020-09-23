package org.jitsi.impl.neomedia;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.ice4j.socket.MultiplexingSocket;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.packetlogging.PacketLoggingService.ProtocolName;
import org.jitsi.service.packetlogging.PacketLoggingService.TransportName;

public class RTPConnectorTCPOutputStream extends RTPConnectorOutputStream {
    private final Socket socket;

    public RTPConnectorTCPOutputStream(Socket socket) {
        this.socket = socket;
    }

    /* access modifiers changed from: protected */
    public void sendToTarget(RawPacket packet, InetSocketAddress target) throws IOException {
        this.socket.getOutputStream().write(packet.getBuffer(), packet.getOffset(), packet.getLength());
    }

    /* access modifiers changed from: protected */
    public void doLogPacket(RawPacket packet, InetSocketAddress target) {
        if (!(this.socket instanceof MultiplexingSocket)) {
            PacketLoggingService packetLogging = LibJitsi.getPacketLoggingService();
            if (packetLogging != null) {
                packetLogging.logPacket(ProtocolName.RTP, this.socket.getLocalAddress().getAddress(), this.socket.getLocalPort(), target.getAddress().getAddress(), target.getPort(), TransportName.TCP, true, packet.getBuffer(), packet.getOffset(), packet.getLength());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isSocketValid() {
        return this.socket != null;
    }
}
