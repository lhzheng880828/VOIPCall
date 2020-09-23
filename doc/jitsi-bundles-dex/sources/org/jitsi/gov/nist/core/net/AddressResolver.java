package org.jitsi.gov.nist.core.net;

import org.jitsi.javax.sip.address.Hop;

public interface AddressResolver {
    Hop resolveAddress(Hop hop);
}
