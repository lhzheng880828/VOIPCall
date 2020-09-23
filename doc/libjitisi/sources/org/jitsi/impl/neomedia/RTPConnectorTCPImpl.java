package org.jitsi.impl.neomedia;

import java.io.IOException;
import java.net.Socket;
import org.jitsi.service.neomedia.StreamConnector;

public class RTPConnectorTCPImpl extends AbstractRTPConnector {
    private Socket controlSocket;
    private Socket dataSocket;

    public RTPConnectorTCPImpl(StreamConnector connector) {
        super(connector);
    }

    public Socket getDataSocket() {
        if (this.dataSocket == null) {
            this.dataSocket = this.connector.getDataTCPSocket();
        }
        return this.dataSocket;
    }

    public Socket getControlSocket() {
        if (this.controlSocket == null) {
            this.controlSocket = this.connector.getControlTCPSocket();
        }
        return this.controlSocket;
    }

    /* access modifiers changed from: protected */
    public RTPConnectorInputStream createControlInputStream() throws IOException {
        return new RTPConnectorTCPInputStream(getControlSocket());
    }

    /* access modifiers changed from: protected */
    public RTPConnectorOutputStream createControlOutputStream() throws IOException {
        return new RTPConnectorTCPOutputStream(getControlSocket());
    }

    /* access modifiers changed from: protected */
    public RTPConnectorInputStream createDataInputStream() throws IOException {
        return new RTPConnectorTCPInputStream(getDataSocket());
    }

    /* access modifiers changed from: protected */
    public RTPConnectorOutputStream createDataOutputStream() throws IOException {
        return new RTPConnectorTCPOutputStream(getDataSocket());
    }
}
