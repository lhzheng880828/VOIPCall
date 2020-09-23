package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.core.HostPort;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.parser.Parser;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.FromHeader;

public final class From extends AddressParametersHeader implements FromHeader {
    private static final long serialVersionUID = -6312727234330643892L;

    public From() {
        super("From");
    }

    public From(To to) {
        super("From");
        this.address = to.address;
        this.parameters = to.parameters;
    }

    /* access modifiers changed from: protected */
    public String encodeBody() {
        return encodeBody(new StringBuilder()).toString();
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder buffer) {
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
        return buffer;
    }

    public HostPort getHostPort() {
        return this.address.getHostPort();
    }

    public String getDisplayName() {
        return this.address.getDisplayName();
    }

    public String getTag() {
        if (this.parameters == null) {
            return null;
        }
        return getParameter("tag");
    }

    public boolean hasTag() {
        return hasParameter("tag");
    }

    public void removeTag() {
        this.parameters.delete("tag");
    }

    public void setAddress(Address address) {
        this.address = (AddressImpl) address;
    }

    public void setTag(String t) throws ParseException {
        Parser.checkToken(t);
        setParameter("tag", t);
    }

    public String getUserAtHostPort() {
        return this.address.getUserAtHostPort();
    }

    public boolean equals(Object other) {
        return (other instanceof FromHeader) && super.equals(other);
    }
}
