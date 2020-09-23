package org.jitsi.javax.sip;

import org.jitsi.javax.sip.message.Response;

public interface ServerTransaction extends Transaction {
    void enableRetransmissionAlerts() throws SipException;

    void sendResponse(Response response) throws SipException, InvalidArgumentException;
}
