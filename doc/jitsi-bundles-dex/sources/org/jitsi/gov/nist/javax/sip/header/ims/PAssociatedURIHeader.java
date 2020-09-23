package org.jitsi.gov.nist.javax.sip.header.ims;

import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.HeaderAddress;
import org.jitsi.javax.sip.header.Parameters;

public interface PAssociatedURIHeader extends HeaderAddress, Parameters, Header {
    public static final String NAME = "P-Associated-URI";

    URI getAssociatedURI();

    void setAssociatedURI(URI uri) throws NullPointerException;
}
