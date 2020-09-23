package org.jitsi.javax.sip;

import java.util.EventObject;
import org.jitsi.javax.sip.message.Response;

public class ResponseEvent extends EventObject {
    private Dialog m_dialog;
    private Response m_response;
    private ClientTransaction m_transaction;

    public ResponseEvent(Object source, ClientTransaction clientTransaction, Dialog dialog, Response response) {
        super(source);
        this.m_response = response;
        this.m_transaction = clientTransaction;
        this.m_dialog = dialog;
    }

    public ClientTransaction getClientTransaction() {
        return this.m_transaction;
    }

    public Response getResponse() {
        return this.m_response;
    }

    public Dialog getDialog() {
        return this.m_dialog;
    }
}
