package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class ExtensionHeaderImpl extends SIPHeader implements ExtensionHeader {
    private static final long serialVersionUID = -8693922839612081849L;
    protected String value;

    public ExtensionHeaderImpl(String headerName) {
        super(headerName);
    }

    public void setName(String headerName) {
        this.headerName = headerName;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getHeaderValue() {
        if (this.value != null) {
            return this.value;
        }
        try {
            StringBuilder buffer = new StringBuilder(encode());
            while (buffer.length() > 0 && buffer.charAt(0) != ':') {
                buffer.deleteCharAt(0);
            }
            buffer.deleteCharAt(0);
            this.value = buffer.toString().trim();
            return this.value;
        } catch (Exception e) {
            return null;
        }
    }

    public String encode() {
        return new StringBuffer(this.headerName).append(Separators.COLON).append(Separators.SP).append(this.value).append(Separators.NEWLINE).toString();
    }

    public StringBuilder encodeBody(StringBuilder buffer) {
        return buffer.append(encodeBody());
    }

    public String encodeBody() {
        return getHeaderValue();
    }
}
