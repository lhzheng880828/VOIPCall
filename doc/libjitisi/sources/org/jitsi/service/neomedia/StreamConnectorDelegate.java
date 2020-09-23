package org.jitsi.service.neomedia;

import java.net.DatagramSocket;
import java.net.Socket;
import org.jitsi.service.neomedia.StreamConnector;
import org.jitsi.service.neomedia.StreamConnector.Protocol;

public class StreamConnectorDelegate<T extends StreamConnector> implements StreamConnector {
    protected final T streamConnector;

    public StreamConnectorDelegate(T streamConnector) {
        if (streamConnector == null) {
            throw new NullPointerException("streamConnector");
        }
        this.streamConnector = streamConnector;
    }

    public void close() {
        this.streamConnector.close();
    }

    public DatagramSocket getControlSocket() {
        return this.streamConnector.getControlSocket();
    }

    public Socket getControlTCPSocket() {
        return this.streamConnector.getControlTCPSocket();
    }

    public DatagramSocket getDataSocket() {
        return this.streamConnector.getDataSocket();
    }

    public Socket getDataTCPSocket() {
        return this.streamConnector.getDataTCPSocket();
    }

    public Protocol getProtocol() {
        return this.streamConnector.getProtocol();
    }

    public void started() {
        this.streamConnector.started();
    }

    public void stopped() {
        this.streamConnector.stopped();
    }
}
