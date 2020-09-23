package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.gov.nist.javax.sip.header.ims.AuthorizationHeaderIms;
import org.jitsi.javax.sip.header.AuthorizationHeader;

public class Authorization extends AuthenticationHeader implements AuthorizationHeader, AuthorizationHeaderIms {
    private static final long serialVersionUID = -8897770321892281348L;

    public Authorization() {
        super("Authorization");
    }
}
