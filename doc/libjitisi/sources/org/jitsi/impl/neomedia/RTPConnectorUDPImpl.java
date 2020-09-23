package org.jitsi.impl.neomedia;

import java.io.IOException;
import java.net.DatagramSocket;
import org.jitsi.service.neomedia.StreamConnector;

public class RTPConnectorUDPImpl extends AbstractRTPConnector {
    private DatagramSocket controlSocket;
    private DatagramSocket dataSocket;

    public RTPConnectorUDPImpl(StreamConnector connector) {
        super(connector);
    }

    public DatagramSocket getDataSocket() {
        if (this.dataSocket == null) {
            this.dataSocket = this.connector.getDataSocket();
        }
        return this.dataSocket;
    }

    public DatagramSocket getControlSocket() {
        if (this.controlSocket == null) {
            this.controlSocket = this.connector.getControlSocket();
        }
        return this.controlSocket;
    }

    /* access modifiers changed from: protected */
    public RTPConnectorInputStream createControlInputStream() throws IOException {
        return new RTCPConnectorInputStream(getControlSocket());
    }

    /* access modifiers changed from: protected */
    public RTPConnectorOutputStream createControlOutputStream() throws IOException {
        return new RTPConnectorUDPOutputStream(getControlSocket());
    }

    /* access modifiers changed from: protected */
    public RTPConnectorInputStream createDataInputStream() throws IOException {
        return new RTPConnectorUDPInputStream(getDataSocket());
    }

    /* access modifiers changed from: protected */
    public RTPConnectorOutputStream createDataOutputStream() throws IOException {
        return new RTPConnectorUDPOutputStream(getDataSocket());
    }
}
