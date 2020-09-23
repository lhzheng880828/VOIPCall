package org.jitsi.gov.nist.javax.sip.stack;

import org.jitsi.gov.nist.javax.sip.message.SIPMessage;

public interface RawMessageChannel {
    void processMessage(SIPMessage sIPMessage) throws Exception;
}
