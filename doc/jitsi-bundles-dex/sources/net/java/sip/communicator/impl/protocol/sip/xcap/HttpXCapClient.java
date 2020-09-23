package net.java.sip.communicator.impl.protocol.sip.xcap;

import java.net.URI;
import org.jitsi.javax.sip.address.Address;

public interface HttpXCapClient {
    void connect(URI uri, Address address, String str, String str2) throws XCapException;

    XCapHttpResponse delete(XCapResourceId xCapResourceId) throws XCapException;

    void disconnect();

    XCapHttpResponse get(XCapResourceId xCapResourceId) throws XCapException;

    URI getUri();

    String getUserName();

    boolean isConnected();

    XCapHttpResponse put(XCapResource xCapResource) throws XCapException;
}
