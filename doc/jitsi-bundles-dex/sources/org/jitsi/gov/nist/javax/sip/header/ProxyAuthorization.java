package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.javax.sip.header.ProxyAuthorizationHeader;

public class ProxyAuthorization extends AuthenticationHeader implements ProxyAuthorizationHeader {
    private static final long serialVersionUID = -6374966905199799098L;

    public ProxyAuthorization() {
        super("Proxy-Authorization");
    }
}
