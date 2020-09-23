package org.jitsi.javax.sip;

import java.util.EventObject;

public class TimeoutEvent extends EventObject {
    private ClientTransaction m_clientTransaction = null;
    private boolean m_isServerTransaction;
    private ServerTransaction m_serverTransaction = null;
    private Timeout m_timeout;

    public TimeoutEvent(Object source, ServerTransaction serverTransaction, Timeout timeout) {
        super(source);
        this.m_serverTransaction = serverTransaction;
        this.m_isServerTransaction = true;
        this.m_timeout = timeout;
    }

    public TimeoutEvent(Object source, ClientTransaction clientTransaction, Timeout timeout) {
        super(source);
        this.m_clientTransaction = clientTransaction;
        this.m_isServerTransaction = false;
        this.m_timeout = timeout;
    }

    public ServerTransaction getServerTransaction() {
        return this.m_serverTransaction;
    }

    public ClientTransaction getClientTransaction() {
        return this.m_clientTransaction;
    }

    public boolean isServerTransaction() {
        return this.m_isServerTransaction;
    }

    public Timeout getTimeout() {
        return this.m_timeout;
    }
}
