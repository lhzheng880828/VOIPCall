package org.jitsi.javax.sip.header;

import org.jitsi.javax.sip.InvalidArgumentException;

public interface ContactHeader extends HeaderAddress, Parameters, Header {
    public static final String NAME = "Contact";

    int getExpires();

    float getQValue();

    boolean isWildCard();

    void setExpires(int i) throws InvalidArgumentException;

    void setQValue(float f) throws InvalidArgumentException;

    void setWildCard();
}
