package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.gov.nist.javax.sip.header.ims.WWWAuthenticateHeaderIms;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.WWWAuthenticateHeader;

public class WWWAuthenticate extends AuthenticationHeader implements WWWAuthenticateHeader, WWWAuthenticateHeaderIms {
    private static final long serialVersionUID = 115378648697363486L;

    public WWWAuthenticate() {
        super("WWW-Authenticate");
    }

    public URI getURI() {
        return null;
    }

    public void setURI(URI uri) {
    }
}
