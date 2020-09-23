package org.jitsi.gov.nist.javax.sip.clientauthutils;

import org.jitsi.javax.sip.ClientTransaction;

public interface AccountManager {
    UserCredentials getCredentials(ClientTransaction clientTransaction, String str);
}
