package javax.media.rtp;

import java.io.Serializable;
import java.net.InetAddress;

public class SessionAddress implements Serializable {
    public static final int ANY_PORT = -1;
    private InetAddress m_controlAddress;
    private int m_controlPort;
    private InetAddress m_dataAddress;
    private int m_dataPort;
    private int ttl;

    public SessionAddress() {
        this.m_dataPort = -1;
        this.m_controlPort = -1;
    }

    public SessionAddress(InetAddress dataAddress, int dataPort) {
        this(dataAddress, dataPort, 0);
    }

    public SessionAddress(InetAddress dataAddress, int dataPort, int timeToLive) {
        this(dataAddress, dataPort, dataAddress, dataPort + 1);
        this.ttl = timeToLive;
    }

    public SessionAddress(InetAddress dataAddress, int dataPort, InetAddress controlAddress, int controlPort) {
        this.m_dataPort = -1;
        this.m_controlPort = -1;
        this.m_dataAddress = dataAddress;
        this.m_dataPort = dataPort;
        this.m_controlAddress = controlAddress;
        this.m_controlPort = controlPort;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof SessionAddress)) {
            return false;
        }
        SessionAddress oCast = (SessionAddress) obj;
        if (getControlAddress().equals(oCast.getControlAddress()) && getDataAddress().equals(oCast.getDataAddress()) && getControlPort() == oCast.getControlPort() && getDataPort() == oCast.getDataPort()) {
            return true;
        }
        return false;
    }

    public InetAddress getControlAddress() {
        return this.m_controlAddress;
    }

    public String getControlHostAddress() {
        return this.m_controlAddress.getHostAddress();
    }

    public int getControlPort() {
        return this.m_controlPort;
    }

    public InetAddress getDataAddress() {
        return this.m_dataAddress;
    }

    public String getDataHostAddress() {
        return this.m_dataAddress.getHostAddress();
    }

    public int getDataPort() {
        return this.m_dataPort;
    }

    public int getTimeToLive() {
        return this.ttl;
    }

    public int hashCode() {
        return ((getControlAddress().hashCode() + getDataAddress().hashCode()) + getControlPort()) + getDataPort();
    }

    public void setControlHostAddress(InetAddress controlAddress) {
        this.m_controlAddress = controlAddress;
    }

    public void setControlPort(int controlPort) {
        this.m_controlPort = controlPort;
    }

    public void setDataHostAddress(InetAddress dataAddress) {
        this.m_dataAddress = dataAddress;
    }

    public void setDataPort(int dataPort) {
        this.m_dataPort = dataPort;
    }

    public String toString() {
        return "DataAddress: " + this.m_dataAddress + "\nControlAddress: " + this.m_controlAddress + "\nDataPort: " + this.m_dataPort + "\nControlPort: " + this.m_controlPort;
    }
}
