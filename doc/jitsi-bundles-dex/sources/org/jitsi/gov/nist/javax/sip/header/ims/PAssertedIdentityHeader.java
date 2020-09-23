package org.jitsi.gov.nist.javax.sip.header.ims;

import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.HeaderAddress;

public interface PAssertedIdentityHeader extends HeaderAddress, Header {
    public static final String NAME = "P-Asserted-Identity";
}
