package org.jitsi.impl.neomedia;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import org.ice4j.socket.MultiplexingSocket;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.packetlogging.PacketLoggingService;
import org.jitsi.service.packetlogging.PacketLoggingService.ProtocolName;
import org.jitsi.service.packetlogging.PacketLoggingService.TransportName;
import org.jitsi.util.Logger;

public class RTPConnectorTCPInputStream extends RTPConnectorInputStream {
    private static final Logger logger = Logger.getLogger(RTPConnectorTCPInputStream.class);
    private final Socket socket;

    public RTPConnectorTCPInputStream(Socket socket) {
        this.socket = socket;
        if (socket != null) {
            try {
                socket.setReceiveBufferSize(65535);
            } catch (Throwable th) {
            }
            this.closed = false;
            this.receiverThread = new Thread(this);
            this.receiverThread.start();
        }
    }

    public synchronized void close() {
        this.closed = true;
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void doLogPacket(DatagramPacket p) {
        if (this.socket.getLocalAddress() != null && !(this.socket instanceof MultiplexingSocket)) {
            PacketLoggingService packetLogging = LibJitsi.getPacketLoggingService();
            if (packetLogging != null) {
                packetLogging.logPacket(ProtocolName.RTP, p.getAddress() != null ? p.getAddress().getAddress() : new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0}, p.getPort(), this.socket.getLocalAddress().getAddress(), this.socket.getLocalPort(), TransportName.TCP, false, p.getData(), p.getOffset(), p.getLength());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void receivePacket(DatagramPacket p) throws IOException {
        int len = -1;
        byte[] data = null;
        try {
            data = p.getData();
            len = this.socket.getInputStream().read(data);
        } catch (Exception e) {
            logger.info("problem read: " + e);
        }
        if (len > 0) {
            p.setData(data);
            p.setLength(len);
            p.setAddress(this.socket.getInetAddress());
            p.setPort(this.socket.getPort());
            return;
        }
        throw new IOException("Failed to read on TCP socket");
    }
}
