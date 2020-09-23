package org.jitsi.javax.sip;

import java.util.EventObject;

public class IOExceptionEvent extends EventObject {
    private String m_host;
    private int m_port;
    private String m_transport;

    public IOExceptionEvent(Object source, String remoteHost, int port, String transport) {
        super(source);
        this.m_host = remoteHost;
        this.m_port = port;
        this.m_transport = transport;
    }

    public String getHost() {
        return this.m_host;
    }

    public int getPort() {
        return this.m_port;
    }

    public String getTransport() {
        return this.m_transport;
    }
}
