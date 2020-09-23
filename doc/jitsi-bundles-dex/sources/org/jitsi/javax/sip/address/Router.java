package org.jitsi.javax.sip.address;

import java.util.ListIterator;
import org.jitsi.javax.sip.SipException;
import org.jitsi.javax.sip.message.Request;

public interface Router {
    Hop getNextHop(Request request) throws SipException;

    ListIterator getNextHops(Request request);

    Hop getOutboundProxy();
}
