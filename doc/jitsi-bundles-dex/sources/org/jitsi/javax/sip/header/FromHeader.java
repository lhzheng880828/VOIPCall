package org.jitsi.javax.sip.header;

import java.text.ParseException;

public interface FromHeader extends HeaderAddress, Parameters, Header {
    public static final String NAME = "From";

    boolean equals(Object obj);

    String getTag();

    void setTag(String str) throws ParseException;
}
