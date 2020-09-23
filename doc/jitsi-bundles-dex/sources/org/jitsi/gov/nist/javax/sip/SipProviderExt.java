package org.jitsi.gov.nist.javax.sip;

import org.jitsi.javax.sip.SipProvider;

public interface SipProviderExt extends SipProvider {
    void setDialogErrorsAutomaticallyHandled();
}
