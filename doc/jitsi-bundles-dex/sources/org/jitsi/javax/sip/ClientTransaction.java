package org.jitsi.javax.sip;

import org.jitsi.javax.sip.message.Request;

public interface ClientTransaction extends Transaction {
    Request createAck() throws SipException;

    Request createCancel() throws SipException;

    void sendRequest() throws SipException;
}
