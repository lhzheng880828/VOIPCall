package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.MimeVersionHeader;

public class MimeVersion extends SIPHeader implements MimeVersionHeader {
    private static final long serialVersionUID = -7951589626435082068L;
    protected int majorVersion;
    protected int minorVersion;

    public MimeVersion() {
        super("MIME-Version");
    }

    public int getMinorVersion() {
        return this.minorVersion;
    }

    public int getMajorVersion() {
        return this.majorVersion;
    }

    public void setMinorVersion(int minorVersion) throws InvalidArgumentException {
        if (minorVersion < 0) {
            throw new InvalidArgumentException("JAIN-SIP Exception, MimeVersion, setMinorVersion(), the minorVersion parameter is null");
        }
        this.minorVersion = minorVersion;
    }

    public void setMajorVersion(int majorVersion) throws InvalidArgumentException {
        if (majorVersion < 0) {
            throw new InvalidArgumentException("JAIN-SIP Exception, MimeVersion, setMajorVersion(), the majorVersion parameter is null");
        }
        this.majorVersion = majorVersion;
    }

    public StringBuilder encodeBody(StringBuilder buffer) {
        return buffer.append(Integer.toString(this.majorVersion)).append(Separators.DOT).append(Integer.toString(this.minorVersion));
    }
}
