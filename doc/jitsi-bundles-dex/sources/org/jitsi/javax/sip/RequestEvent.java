package org.jitsi.javax.sip;

import java.util.EventObject;
import org.jitsi.javax.sip.message.Request;

public class RequestEvent extends EventObject {
    private Dialog m_dialog;
    private Request m_request;
    private ServerTransaction m_transaction;

    public RequestEvent(Object source, ServerTransaction serverTransaction, Dialog dialog, Request request) {
        super(source);
        this.m_transaction = serverTransaction;
        this.m_request = request;
        this.m_dialog = dialog;
    }

    public ServerTransaction getServerTransaction() {
        return this.m_transaction;
    }

    public Request getRequest() {
        return this.m_request;
    }

    public Dialog getDialog() {
        return this.m_dialog;
    }
}
