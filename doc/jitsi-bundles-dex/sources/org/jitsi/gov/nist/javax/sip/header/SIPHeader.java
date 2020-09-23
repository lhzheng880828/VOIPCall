package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.header.Header;

public abstract class SIPHeader extends SIPObject implements SIPHeaderNames, Header, HeaderExt {
    protected String headerName;

    public abstract StringBuilder encodeBody(StringBuilder stringBuilder);

    protected SIPHeader(String hname) {
        this.headerName = hname.intern();
    }

    public String getHeaderName() {
        return this.headerName;
    }

    public String getName() {
        return this.headerName;
    }

    public void setHeaderName(String hdrname) {
        this.headerName = hdrname;
    }

    public String getHeaderValue() {
        return encodeBody(new StringBuilder()).toString();
    }

    public boolean isHeaderList() {
        return false;
    }

    public String encode() {
        return encode(new StringBuilder()).toString();
    }

    public StringBuilder encode(StringBuilder buffer) {
        buffer.append(this.headerName).append(Separators.COLON).append(Separators.SP);
        encodeBody(buffer);
        buffer.append(Separators.NEWLINE);
        return buffer;
    }

    public String getValue() {
        return getHeaderValue();
    }

    public int hashCode() {
        return this.headerName.hashCode();
    }

    public final String toString() {
        return encode();
    }
}
