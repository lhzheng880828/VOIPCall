package org.jitsi.javax.sip.header;

import java.text.ParseException;
import org.jitsi.javax.sip.InvalidArgumentException;

public interface RetryAfterHeader extends Header, Parameters {
    public static final String NAME = "Retry-After";

    String getComment();

    int getDuration();

    int getRetryAfter();

    void setComment(String str) throws ParseException;

    void setDuration(int i) throws InvalidArgumentException;

    void setRetryAfter(int i) throws InvalidArgumentException;
}
