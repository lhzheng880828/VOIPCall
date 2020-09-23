package org.jitsi.javax.sip.header;

import java.text.ParseException;

public interface InReplyToHeader extends Header {
    public static final String NAME = "In-Reply-To";

    String getCallId();

    void setCallId(String str) throws ParseException;
}
