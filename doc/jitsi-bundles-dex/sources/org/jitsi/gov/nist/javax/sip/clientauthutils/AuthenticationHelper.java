package org.jitsi.gov.nist.javax.sip.clientauthutils;

import org.jitsi.javax.sip.ClientTransaction;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.SipProvider;
import org.jitsi.javax.sip.message.Request;
import org.jitsi.javax.sip.message.Response;

public interface AuthenticationHelper {
    ClientTransaction handleChallenge(Response response, ClientTransaction clientTransaction, SipProvider sipProvider, int i) throws SipException, NullPointerException;

    void removeCachedAuthenticationHeaders(String str);

    void setAuthenticationHeaders(Request request);
}
