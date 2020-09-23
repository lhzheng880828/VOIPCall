package org.jitsi.impl.neomedia;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import org.ice4j.socket.MultiplexingDatagramSocket;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.packetlogging.PacketLoggingService.ProtocolName;
import org.jitsi.service.packetlogging.PacketLoggingService.TransportName;

public class RTPConnectorUDPInputStream extends RTPConnectorInputStream {
    private boolean setReceiveBufferSize = false;
    private final DatagramSocket socket;

    public RTPConnectorUDPInputStream(DatagramSocket socket) {
        this.socket = socket;
        if (socket != null) {
            this.closed = false;
            this.receiverThread = new Thread(this);
            this.receiverThread.start();
        }
    }

    public synchronized void close() {
        this.closed = true;
        if (this.socket != null) {
            this.socket.close();
        }
    }

    /* access modifiers changed from: protected */
    public void doLogPacket(DatagramPacket p) {
        if (this.socket.getLocalAddress() != null && !(this.socket instanceof MultiplexingDatagramSocket)) {
            PacketLoggingService packetLogging = LibJitsi.getPacketLoggingService();
            if (packetLogging != null) {
                packetLogging.logPacket(ProtocolName.RTP, p.getAddress().getAddress(), p.getPort(), this.socket.getLocalAddress().getAddress(), this.socket.getLocalPort(), TransportName.UDP, false, p.getData(), p.getOffset(), p.getLength());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void receivePacket(DatagramPacket p) throws IOException {
        if (!this.setReceiveBufferSize) {
            this.setReceiveBufferSize = true;
            try {
                this.socket.setReceiveBufferSize(65535);
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    ThreadDeath t2 = (ThreadDeath) t;
                }
            }
        }
        this.socket.receive(p);
    }
}
