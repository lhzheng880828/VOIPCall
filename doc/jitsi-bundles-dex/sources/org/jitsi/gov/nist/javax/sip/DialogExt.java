package org.jitsi.gov.nist.javax.sip;

import org.jitsi.javax.sip.Dialog;
import org.jitsi.javax.sip.SipProvider;

public interface DialogExt extends Dialog {
    void disableSequenceNumberValidation();

    SipProvider getSipProvider();

    boolean isReleaseReferences();

    void setBackToBackUserAgent();

    void setEarlyDialogTimeoutSeconds(int i);

    void setReleaseReferences(boolean z);
}
