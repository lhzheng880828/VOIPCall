package org.jitsi.service.neomedia;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import org.jitsi.service.neomedia.StreamConnector.Protocol;
import org.jitsi.util.Logger;

public class DefaultTCPStreamConnector implements StreamConnector {
    private static final Logger logger = Logger.getLogger(DefaultTCPStreamConnector.class);
    protected Socket controlSocket;
    protected Socket dataSocket;

    public DefaultTCPStreamConnector() {
        this(null, null);
    }

    public DefaultTCPStreamConnector(Socket dataSocket, Socket controlSocket) {
        this.controlSocket = controlSocket;
        this.dataSocket = dataSocket;
    }

    public void close() {
        try {
            if (this.controlSocket != null) {
                this.controlSocket.close();
            }
            if (this.dataSocket != null) {
                this.dataSocket.close();
            }
        } catch (IOException ioe) {
            logger.debug("Failed to close TCP socket", ioe);
        }
    }

    public DatagramSocket getControlSocket() {
        return null;
    }

    public DatagramSocket getDataSocket() {
        return null;
    }

    public Socket getDataTCPSocket() {
        return this.dataSocket;
    }

    public Socket getControlTCPSocket() {
        return this.controlSocket;
    }

    public Protocol getProtocol() {
        return Protocol.TCP;
    }

    public void started() {
    }

    public void stopped() {
    }
}
