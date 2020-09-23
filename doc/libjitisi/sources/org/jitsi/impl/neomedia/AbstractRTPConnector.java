package org.jitsi.impl.neomedia;

import java.io.IOException;
import java.net.InetAddress;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.SessionAddress;
import org.jitsi.service.neomedia.StreamConnector;

public abstract class AbstractRTPConnector implements RTPConnector {
    protected final StreamConnector connector;
    private RTPConnectorInputStream controlInputStream;
    private RTPConnectorOutputStream controlOutputStream;
    private RTPConnectorInputStream dataInputStream;
    private RTPConnectorOutputStream dataOutputStream;

    public abstract RTPConnectorInputStream createControlInputStream() throws IOException;

    public abstract RTPConnectorOutputStream createControlOutputStream() throws IOException;

    public abstract RTPConnectorInputStream createDataInputStream() throws IOException;

    public abstract RTPConnectorOutputStream createDataOutputStream() throws IOException;

    public AbstractRTPConnector(StreamConnector connector) {
        if (connector == null) {
            throw new NullPointerException("connector");
        }
        this.connector = connector;
    }

    public void addTarget(SessionAddress target) throws IOException {
        InetAddress controlAddress = target.getControlAddress();
        if (controlAddress != null) {
            getControlOutputStream().addTarget(controlAddress, target.getControlPort());
        }
        getDataOutputStream().addTarget(target.getDataAddress(), target.getDataPort());
    }

    public void close() {
        if (this.dataOutputStream != null) {
            this.dataOutputStream.close();
            this.dataOutputStream = null;
        }
        if (this.controlOutputStream != null) {
            this.controlOutputStream.close();
            this.controlOutputStream = null;
        }
        if (this.dataInputStream != null) {
            this.dataInputStream.close();
            this.dataInputStream = null;
        }
        if (this.controlInputStream != null) {
            this.controlInputStream.close();
            this.controlInputStream = null;
        }
        this.connector.close();
    }

    public final StreamConnector getConnector() {
        return this.connector;
    }

    public RTPConnectorInputStream getControlInputStream() throws IOException {
        return getControlInputStream(true);
    }

    /* access modifiers changed from: protected */
    public RTPConnectorInputStream getControlInputStream(boolean create) throws IOException {
        if (this.controlInputStream == null && create) {
            this.controlInputStream = createControlInputStream();
        }
        return this.controlInputStream;
    }

    public RTPConnectorOutputStream getControlOutputStream() throws IOException {
        return getControlOutputStream(true);
    }

    /* access modifiers changed from: protected */
    public RTPConnectorOutputStream getControlOutputStream(boolean create) throws IOException {
        if (this.controlOutputStream == null && create) {
            this.controlOutputStream = createControlOutputStream();
        }
        return this.controlOutputStream;
    }

    public RTPConnectorInputStream getDataInputStream() throws IOException {
        return getDataInputStream(true);
    }

    /* access modifiers changed from: protected */
    public RTPConnectorInputStream getDataInputStream(boolean create) throws IOException {
        if (this.dataInputStream == null && create) {
            this.dataInputStream = createDataInputStream();
        }
        return this.dataInputStream;
    }

    public RTPConnectorOutputStream getDataOutputStream() throws IOException {
        return getDataOutputStream(true);
    }

    public RTPConnectorOutputStream getDataOutputStream(boolean create) throws IOException {
        if (this.dataOutputStream == null && create) {
            this.dataOutputStream = createDataOutputStream();
        }
        return this.dataOutputStream;
    }

    public int getReceiveBufferSize() {
        return -1;
    }

    public double getRTCPBandwidthFraction() {
        return -1.0d;
    }

    public double getRTCPSenderBandwidthFraction() {
        return -1.0d;
    }

    public int getSendBufferSize() {
        return -1;
    }

    public void removeTarget(SessionAddress target) {
        if (this.controlOutputStream != null) {
            this.controlOutputStream.removeTarget(target.getControlAddress(), target.getControlPort());
        }
        if (this.dataOutputStream != null) {
            this.dataOutputStream.removeTarget(target.getDataAddress(), target.getDataPort());
        }
    }

    public void removeTargets() {
        if (this.controlOutputStream != null) {
            this.controlOutputStream.removeTargets();
        }
        if (this.dataOutputStream != null) {
            this.dataOutputStream.removeTargets();
        }
    }

    public void setReceiveBufferSize(int size) throws IOException {
    }

    public void setSendBufferSize(int size) throws IOException {
    }
}
