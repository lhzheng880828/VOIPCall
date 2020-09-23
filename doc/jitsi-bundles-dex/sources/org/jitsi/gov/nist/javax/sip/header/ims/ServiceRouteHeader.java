package org.jitsi.gov.nist.javax.sip.header.ims;

import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.HeaderAddress;
import org.jitsi.javax.sip.header.Parameters;

public interface ServiceRouteHeader extends HeaderAddress, Parameters, Header {
    public static final String NAME = "Service-Route";
}
