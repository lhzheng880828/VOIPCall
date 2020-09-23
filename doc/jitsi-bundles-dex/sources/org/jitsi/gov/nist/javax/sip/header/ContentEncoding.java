package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.javax.sip.header.ContentEncodingHeader;

public class ContentEncoding extends SIPHeader implements ContentEncodingHeader {
    private static final long serialVersionUID = 2034230276579558857L;
    protected String contentEncoding;

    public ContentEncoding() {
        super("Content-Encoding");
    }

    public ContentEncoding(String enc) {
        super("Content-Encoding");
        this.contentEncoding = enc;
    }

    public StringBuilder encodeBody(StringBuilder buffer) {
        return buffer.append(this.contentEncoding);
    }

    public String getEncoding() {
        return this.contentEncoding;
    }

    public void setEncoding(String encoding) throws ParseException {
        if (encoding == null) {
            throw new NullPointerException("JAIN-SIP Exception,  encoding is null");
        }
        this.contentEncoding = encoding;
    }
}
