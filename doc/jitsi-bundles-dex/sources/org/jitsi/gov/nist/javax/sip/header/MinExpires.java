package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.MinExpiresHeader;

public class MinExpires extends SIPHeader implements MinExpiresHeader {
    private static final long serialVersionUID = 7001828209606095801L;
    protected int expires;

    public MinExpires() {
        super("Min-Expires");
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        return retval.append(Integer.toString(this.expires));
    }

    public int getExpires() {
        return this.expires;
    }

    public void setExpires(int expires) throws InvalidArgumentException {
        if (expires < 0) {
            throw new InvalidArgumentException("bad argument " + expires);
        }
        this.expires = expires;
    }
}
