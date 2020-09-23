package org.jitsi.gov.nist.javax.sip.header.extensions;

import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.HeaderAddress;
import org.jitsi.javax.sip.header.Parameters;

public interface ReferredByHeader extends HeaderAddress, Parameters, Header {
    public static final String NAME = "Referred-By";
}
