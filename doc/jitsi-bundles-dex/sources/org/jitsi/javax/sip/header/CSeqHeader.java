package org.jitsi.javax.sip.header;

import java.text.ParseException;
import org.jitsi.javax.sip.InvalidArgumentException;

public interface CSeqHeader extends Header {
    public static final String NAME = "CSeq";

    boolean equals(Object obj);

    String getMethod();

    long getSeqNumber();

    int getSequenceNumber();

    void setMethod(String str) throws ParseException;

    void setSeqNumber(long j) throws InvalidArgumentException;

    void setSequenceNumber(int i) throws InvalidArgumentException;
}
