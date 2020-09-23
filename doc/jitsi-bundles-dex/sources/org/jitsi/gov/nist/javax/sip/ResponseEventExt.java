package org.jitsi.gov.nist.javax.sip;

import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.ResponseEvent;
import org.jitsi.javax.sip.message.Response;

public class ResponseEventExt extends ResponseEvent {
    private boolean isForked;
    private boolean isRetransmission;
    private ClientTransactionExt m_originalTransaction;
    private String remoteIpAddress;
    private int remotePort;

    public ResponseEventExt(Object source, ClientTransactionExt clientTransaction, Dialog dialog, Response response) {
        super(source, clientTransaction, dialog, response);
        this.m_originalTransaction = clientTransaction;
    }

    public boolean isForkedResponse() {
        return this.isForked;
    }

    public void setForkedResponse(boolean forked) {
        this.isForked = forked;
    }

    public void setOriginalTransaction(ClientTransactionExt originalTransaction) {
        this.m_originalTransaction = originalTransaction;
    }

    public ClientTransactionExt getOriginalTransaction() {
        return this.m_originalTransaction;
    }

    public boolean isRetransmission() {
        return this.isRetransmission;
    }

    public void setRetransmission(boolean isRetransmission) {
        this.isRetransmission = isRetransmission;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public int getRemotePort() {
        return this.remotePort;
    }

    public String getRemoteIpAddress() {
        return this.remoteIpAddress;
    }

    public void setRemoteIpAddress(String remoteIpAddress) {
        this.remoteIpAddress = remoteIpAddress;
    }
}
