package org.jitsi.gov.nist.javax.sip.parser;

import org.jitsi.gov.nist.javax.sip.message.SIPMessage;

public interface SIPMessageListener extends ParseExceptionListener {
    void processMessage(SIPMessage sIPMessage) throws Exception;
}
