package org.jitsi.gov.nist.javax.sip.stack;

import org.jitsi.gov.nist.javax.sip.ClientTransactionExt;
import org.jitsi.gov.nist.javax.sip.TlsSecurityPolicy;

public class DefaultTlsSecurityPolicy implements TlsSecurityPolicy {
    public void enforceTlsPolicy(ClientTransactionExt transaction) throws SecurityException {
    }
}
