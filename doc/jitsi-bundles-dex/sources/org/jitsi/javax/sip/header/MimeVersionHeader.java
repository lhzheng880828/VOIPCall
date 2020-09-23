package org.jitsi.javax.sip.header;

import org.jitsi.javax.sip.InvalidArgumentException;

public interface MimeVersionHeader extends Header {
    public static final String NAME = "MIME-Version";

    int getMajorVersion();

    int getMinorVersion();

    void setMajorVersion(int i) throws InvalidArgumentException;

    void setMinorVersion(int i) throws InvalidArgumentException;
}
