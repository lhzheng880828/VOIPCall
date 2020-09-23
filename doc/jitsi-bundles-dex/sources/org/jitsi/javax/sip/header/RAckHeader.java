package org.jitsi.javax.sip.header;

import java.text.ParseException;
import org.jitsi.javax.sip.InvalidArgumentException;

public interface RAckHeader extends Header {
    public static final String NAME = "RAck";

    int getCSeqNumber();

    String getMethod();

    int getRSeqNumber();

    void setCSeqNumber(int i) throws InvalidArgumentException;

    void setMethod(String str) throws ParseException;

    void setRSeqNumber(int i) throws InvalidArgumentException;
}
