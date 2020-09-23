package org.jitsi.gov.nist.javax.sip.header.ims;

import org.jitsi.gov.nist.core.Token;
import org.jitsi.javax.sip.header.Header;
import org.jitsi.javax.sip.header.Parameters;

public interface PVisitedNetworkIDHeader extends Parameters, Header {
    public static final String NAME = "P-Visited-Network-ID";

    String getVisitedNetworkID();

    void setVisitedNetworkID(String str);

    void setVisitedNetworkID(Token token);
}
