package org.jitsi.gov.nist.javax.sip;

import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.RequestEvent;
import org.jitsi.javax.sip.ServerTransaction;
import org.jitsi.javax.sip.message.Request;

public class RequestEventExt extends RequestEvent {
    private String remoteIpAddress;
    private int remotePort;

    public RequestEventExt(Object source, ServerTransaction serverTransaction, Dialog dialog, Request request) {
        super(source, serverTransaction, dialog, request);
    }

    public void setRemoteIpAddress(String remoteIpAddress) {
        this.remoteIpAddress = remoteIpAddress;
    }

    public String getRemoteIpAddress() {
        return this.remoteIpAddress;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public int getRemotePort() {
        return this.remotePort;
    }
}
