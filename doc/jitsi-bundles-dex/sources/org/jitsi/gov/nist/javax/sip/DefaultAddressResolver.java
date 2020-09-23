package org.jitsi.gov.nist.javax.sip;

import org.jitsi.gov.nist.core.net.AddressResolver;
import org.jitsi.gov.nist.javax.sip.stack.HopImpl;
import org.jitsi.gov.nist.javax.sip.stack.MessageProcessor;
import org.jitsi.javax.sip.address.Hop;

public class DefaultAddressResolver implements AddressResolver {
    public Hop resolveAddress(Hop inputAddress) {
        return inputAddress.getPort() != -1 ? inputAddress : new HopImpl(inputAddress.getHost(), MessageProcessor.getDefaultPort(inputAddress.getTransport()), inputAddress.getTransport());
    }
}
