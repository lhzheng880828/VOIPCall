package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.MaxForwardsHeader;
import org.jitsi.javax.sip.header.TooManyHopsException;

public class MaxForwards extends SIPHeader implements MaxForwardsHeader {
    private static final long serialVersionUID = -3096874323347175943L;
    protected int maxForwards;

    public MaxForwards() {
        super("Max-Forwards");
    }

    public MaxForwards(int m) throws InvalidArgumentException {
        super("Max-Forwards");
        setMaxForwards(m);
    }

    public int getMaxForwards() {
        return this.maxForwards;
    }

    public void setMaxForwards(int maxForwards) throws InvalidArgumentException {
        if (maxForwards < 0 || maxForwards > 255) {
            throw new InvalidArgumentException("bad max forwards value " + maxForwards);
        }
        this.maxForwards = maxForwards;
    }

    public String encodeBody() {
        return encodeBody(new StringBuilder()).toString();
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder buffer) {
        return buffer.append(this.maxForwards);
    }

    public boolean hasReachedZero() {
        return this.maxForwards == 0;
    }

    public void decrementMaxForwards() throws TooManyHopsException {
        if (this.maxForwards > 0) {
            this.maxForwards--;
            return;
        }
        throw new TooManyHopsException("has already reached 0!");
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MaxForwardsHeader)) {
            return false;
        }
        if (getMaxForwards() != ((MaxForwardsHeader) other).getMaxForwards()) {
            return false;
        }
        return true;
    }
}
