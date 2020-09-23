package org.jitsi.javax.sip.address;

public interface Hop {
    String getHost();

    int getPort();

    String getTransport();

    String toString();
}
