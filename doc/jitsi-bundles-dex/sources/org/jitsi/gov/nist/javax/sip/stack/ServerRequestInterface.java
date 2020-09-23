package org.jitsi.gov.nist.javax.sip.stack;

import org.jitsi.gov.nist.javax.sip.message.SIPRequest;

public interface ServerRequestInterface {
    void processRequest(SIPRequest sIPRequest, MessageChannel messageChannel);
}
