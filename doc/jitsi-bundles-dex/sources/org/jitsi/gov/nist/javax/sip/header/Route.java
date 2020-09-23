package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.javax.sip.header.RouteHeader;

public class Route extends AddressParametersHeader implements RouteHeader {
    private static final long serialVersionUID = 5683577362998368846L;

    public Route() {
        super("Route");
    }

    public Route(AddressImpl address) {
        super("Route");
        this.address = address;
    }

    public int hashCode() {
        return this.address.getHostPort().encode().toLowerCase().hashCode();
    }

    public String encodeBody() {
        return encodeBody(new StringBuilder()).toString();
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder buffer) {
        boolean addrFlag = true;
        if (this.address.getAddressType() != 1) {
            addrFlag = false;
        }
        if (addrFlag) {
            this.address.encode(buffer);
        } else {
            buffer.append('<');
            this.address.encode(buffer);
            buffer.append('>');
        }
        if (!this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }

    public boolean equals(Object other) {
        return (other instanceof RouteHeader) && super.equals(other);
    }
}
