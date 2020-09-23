package org.jitsi.gov.nist.javax.sip.header;

import javax.sdp.SdpConstants;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.ContentLengthHeader;

public class ContentLength extends SIPHeader implements ContentLengthHeader {
    private static final long serialVersionUID = 1187190542411037027L;
    protected int contentLength = -1;

    public ContentLength() {
        super("Content-Length");
    }

    public ContentLength(int length) {
        super("Content-Length");
        this.contentLength = length;
    }

    public int getContentLength() {
        return this.contentLength;
    }

    public void setContentLength(int contentLength) throws InvalidArgumentException {
        if (contentLength < 0) {
            throw new InvalidArgumentException("JAIN-SIP Exception, ContentLength, setContentLength(), the contentLength parameter is <0");
        }
        this.contentLength = Integer.valueOf(contentLength).intValue();
    }

    public String encodeBody() {
        return encodeBody(new StringBuilder()).toString();
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder buffer) {
        if (this.contentLength < 0) {
            buffer.append(SdpConstants.RESERVED);
        } else {
            buffer.append(this.contentLength);
        }
        return buffer;
    }

    public boolean match(Object other) {
        if (other instanceof ContentLength) {
            return true;
        }
        return false;
    }

    public boolean equals(Object other) {
        if (!(other instanceof ContentLengthHeader)) {
            return false;
        }
        if (getContentLength() == ((ContentLengthHeader) other).getContentLength()) {
            return true;
        }
        return false;
    }
}
