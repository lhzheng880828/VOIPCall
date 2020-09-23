package org.jitsi.gov.nist.javax.sip.stack;

import org.jitsi.javax.sip.SipStack;
import org.jitsi.javax.sip.message.Message;

public interface SIPEventInterceptor {
    void afterMessage(Message message);

    void beforeMessage(Message message);

    void destroy();

    void init(SipStack sipStack);
}
