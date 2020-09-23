package org.jitsi.gov.nist.javax.sip.stack;

import org.jitsi.gov.nist.javax.sip.message.SIPResponse;

public interface ServerResponseInterface {
    void processResponse(SIPResponse sIPResponse, MessageChannel messageChannel);

    void processResponse(SIPResponse sIPResponse, MessageChannel messageChannel, SIPDialog sIPDialog);
}
