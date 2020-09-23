package org.jitsi.gov.nist.javax.sip;

import java.util.EventObject;
import org.jitsi.gov.nist.javax.sip.stack.SIPTransaction;

class EventWrapper {
    protected EventObject sipEvent;
    protected SIPTransaction transaction;

    EventWrapper(EventObject sipEvent, SIPTransaction transaction) {
        this.sipEvent = sipEvent;
        this.transaction = transaction;
    }
}
