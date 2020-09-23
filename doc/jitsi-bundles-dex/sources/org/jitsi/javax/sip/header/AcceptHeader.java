package org.jitsi.javax.sip.header;

import org.jitsi.javax.sip.InvalidArgumentException;

public interface AcceptHeader extends MediaType, Parameters, Header {
    public static final String NAME = "Accept";

    boolean allowsAllContentSubTypes();

    boolean allowsAllContentTypes();

    float getQValue();

    void setQValue(float f) throws InvalidArgumentException;
}
