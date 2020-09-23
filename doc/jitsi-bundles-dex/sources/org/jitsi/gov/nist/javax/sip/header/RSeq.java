package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.RSeqHeader;

public class RSeq extends SIPHeader implements RSeqHeader {
    private static final long serialVersionUID = 8765762413224043394L;
    protected long sequenceNumber;

    public RSeq() {
        super("RSeq");
    }

    public int getSequenceNumber() {
        return (int) this.sequenceNumber;
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        return retval.append(Long.toString(this.sequenceNumber));
    }

    public long getSeqNumber() {
        return this.sequenceNumber;
    }

    public void setSeqNumber(long sequenceNumber) throws InvalidArgumentException {
        if (sequenceNumber <= 0 || sequenceNumber > 2147483648L) {
            throw new InvalidArgumentException("Bad seq number " + sequenceNumber);
        }
        this.sequenceNumber = sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) throws InvalidArgumentException {
        setSeqNumber((long) sequenceNumber);
    }
}
