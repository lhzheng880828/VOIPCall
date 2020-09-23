package org.jitsi.javax.sip.header;

import org.jitsi.javax.sip.InvalidArgumentException;

public interface RSeqHeader extends Header {
    public static final String NAME = "RSeq";

    int getSequenceNumber();

    void setSequenceNumber(int i) throws InvalidArgumentException;
}
