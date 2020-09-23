package org.jitsi.javax.sip.header;

import org.jitsi.javax.sip.InvalidArgumentException;

public interface MaxForwardsHeader extends Header {
    public static final String NAME = "Max-Forwards";

    void decrementMaxForwards() throws TooManyHopsException;

    boolean equals(Object obj);

    int getMaxForwards();

    void setMaxForwards(int i) throws InvalidArgumentException;
}
