package org.jitsi.javax.sip.header;

import org.jitsi.javax.sip.InvalidArgumentException;

public interface ContentLengthHeader extends Header {
    public static final String NAME = "Content-Length";

    int getContentLength();

    void setContentLength(int i) throws InvalidArgumentException;
}
