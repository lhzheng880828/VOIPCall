package org.jitsi.gov.nist.javax.sip;

import org.jitsi.javax.sip.SipListener;

public interface SipListenerExt extends SipListener {
    void processDialogTimeout(DialogTimeoutEvent dialogTimeoutEvent);
}
