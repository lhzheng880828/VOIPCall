package org.jitsi.javax.sip.header;

import java.text.ParseException;

public interface CallIdHeader extends Header {
    public static final String NAME = "Call-ID";

    boolean equals(Object obj);

    String getCallId();

    void setCallId(String str) throws ParseException;
}
