package org.jitsi.gov.nist.javax.sip.stack;

import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.javax.sip.SipStack;
import org.jitsi.javax.sip.message.Response;

public interface SIPMessageValve {
    void destroy();

    void init(SipStack sipStack);

    boolean processRequest(SIPRequest sIPRequest, MessageChannel messageChannel);

    boolean processResponse(Response response, MessageChannel messageChannel);
}
