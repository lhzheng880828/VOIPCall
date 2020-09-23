package org.jitsi.gov.nist.javax.sip;

public interface TlsSecurityPolicy {
    void enforceTlsPolicy(ClientTransactionExt clientTransactionExt) throws SecurityException;
}
