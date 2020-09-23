package org.jitsi.gov.nist.javax.sip.address;

import org.jitsi.javax.sip.address.Hop;
import org.jitsi.javax.sip.address.Router;

public interface RouterExt extends Router {
    void transactionTimeout(Hop hop);
}
