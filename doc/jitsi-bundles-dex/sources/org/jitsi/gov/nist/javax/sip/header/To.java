package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.core.HostPort;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.parser.Parser;
import org.jitsi.javax.sip.header.ToHeader;

public final class To extends AddressParametersHeader implements ToHeader {
    private static final long serialVersionUID = -4057413800584586316L;

    public To() {
        super("To", true);
    }

    public To(From from) {
        super("To");
        setAddress(from.address);
        setParameters(from.parameters);
    }

    public String encode() {
        return this.headerName + Separators.COLON + Separators.SP + encodeBody() + Separators.NEWLINE;
    }

    /* access modifiers changed from: protected */
    public String encodeBody() {
        return encodeBody(new StringBuilder()).toString();
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder buffer) {
        if (this.address != null) {
            if (this.address.getAddressType() == 2) {
                buffer.append(Separators.LESS_THAN);
            }
            this.address.encode(buffer);
            if (this.address.getAddressType() == 2) {
                buffer.append(Separators.GREATER_THAN);
            }
            if (!this.parameters.isEmpty()) {
                buffer.append(Separators.SEMICOLON);
                this.parameters.encode(buffer);
            }
        }
        return buffer;
    }

    public HostPort getHostPort() {
        if (this.address == null) {
            return null;
        }
        return this.address.getHostPort();
    }

    public String getDisplayName() {
        if (this.address == null) {
            return null;
        }
        return this.address.getDisplayName();
    }

    public String getTag() {
        if (this.parameters == null) {
            return null;
        }
        return getParameter("tag");
    }

    public boolean hasTag() {
        if (this.parameters == null) {
            return false;
        }
        return hasParameter("tag");
    }

    public void removeTag() {
        if (this.parameters != null) {
            this.parameters.delete("tag");
        }
    }

    public void setTag(String t) throws ParseException {
        Parser.checkToken(t);
        setParameter("tag", t);
    }

    public String getUserAtHostPort() {
        if (this.address == null) {
            return null;
        }
        return this.address.getUserAtHostPort();
    }

    public boolean equals(Object other) {
        return (other instanceof ToHeader) && super.equals(other);
    }
}
