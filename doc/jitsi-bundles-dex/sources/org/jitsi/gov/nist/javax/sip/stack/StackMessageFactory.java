package org.jitsi.gov.nist.javax.sip.stack;

import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;

public interface StackMessageFactory {
    ServerRequestInterface newSIPServerRequest(SIPRequest sIPRequest, MessageChannel messageChannel);

    ServerResponseInterface newSIPServerResponse(SIPResponse sIPResponse, MessageChannel messageChannel);
}
