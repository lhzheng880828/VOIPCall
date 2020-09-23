package org.jitsi.javax.sip.header;

import org.jitsi.javax.sip.InvalidArgumentException;

public interface AcceptEncodingHeader extends Parameters, Encoding, Header {
    public static final String NAME = "Accept-Encoding";

    float getQValue();

    void setQValue(float f) throws InvalidArgumentException;
}
