package org.jitsi.gov.nist.javax.sip.clientauthutils;

import org.jitsi.javax.sip.ClientTransaction;

public interface SecureAccountManager {
    UserCredentialHash getCredentialHash(ClientTransaction clientTransaction, String str);
}
