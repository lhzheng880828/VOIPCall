package org.jitsi.gov.nist.javax.sip.header.extensions;

import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.ExtensionHeader;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.Parameters;

public interface SessionExpiresHeader extends Parameters, Header, ExtensionHeader {
    public static final String NAME = "Session-Expires";

    int getExpires();

    String getRefresher();

    void setExpires(int i) throws InvalidArgumentException;

    void setRefresher(String str);
}
