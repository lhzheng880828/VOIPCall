package org.jitsi.gov.nist.javax.sip.header.ims;

import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.Header;

public interface PMediaAuthorizationHeader extends Header {
    public static final String NAME = "P-Media-Authorization";

    String getToken();

    void setMediaAuthorizationToken(String str) throws InvalidArgumentException;
}
