package org.jitsi.javax.sip;

import java.io.Serializable;
import java.text.ParseException;

public interface ListeningPoint extends Cloneable, Serializable {
    public static final int PORT_5060 = 5060;
    public static final int PORT_5061 = 5061;
    public static final String SCTP = "SCTP";
    public static final String TCP = "TCP";
    public static final String TLS = "TLS";
    public static final String UDP = "UDP";

    boolean equals(Object obj);

    String getIPAddress();

    int getPort();

    String getSentBy();

    String getTransport();

    void setSentBy(String str) throws ParseException;
}
