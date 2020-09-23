package org.jitsi.javax.sip.header;

import org.jitsi.javax.sip.InvalidArgumentException;

public interface TimeStampHeader extends Header {
    public static final String NAME = "Timestamp";

    float getDelay();

    long getTime();

    int getTimeDelay();

    float getTimeStamp();

    void setDelay(float f) throws InvalidArgumentException;

    void setTime(long j) throws InvalidArgumentException;

    void setTimeDelay(int i) throws InvalidArgumentException;

    void setTimeStamp(float f) throws InvalidArgumentException;
}
