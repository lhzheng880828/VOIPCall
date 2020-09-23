package org.jitsi.javax.sip;

import java.util.EventObject;

public class TransactionTerminatedEvent extends EventObject {
    private ClientTransaction m_clientTransaction = null;
    private boolean m_isServerTransaction;
    private ServerTransaction m_serverTransaction = null;

    public TransactionTerminatedEvent(Object source, ServerTransaction serverTransaction) {
        super(source);
        this.m_serverTransaction = serverTransaction;
        this.m_isServerTransaction = true;
    }

    public TransactionTerminatedEvent(Object source, ClientTransaction clientTransaction) {
        super(source);
        this.m_clientTransaction = clientTransaction;
        this.m_isServerTransaction = false;
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
}
