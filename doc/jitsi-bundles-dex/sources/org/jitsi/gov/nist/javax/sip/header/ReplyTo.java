package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.gov.nist.core.HostPort;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.javax.sip.header.ReplyToHeader;

public final class ReplyTo extends AddressParametersHeader implements ReplyToHeader {
    private static final long serialVersionUID = -9103698729465531373L;

    public ReplyTo() {
        super("Reply-To");
    }

    public ReplyTo(AddressImpl address) {
        super("Reply-To");
        this.address = address;
    }

    public String encode() {
        return this.headerName + Separators.COLON + Separators.SP + encodeBody(new StringBuilder()).toString() + Separators.NEWLINE;
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        if (this.address.getAddressType() == 2) {
            retval.append(Separators.LESS_THAN);
        }
        this.address.encode(retval);
        if (this.address.getAddressType() == 2) {
            retval.append(Separators.GREATER_THAN);
        }
        if (!this.parameters.isEmpty()) {
            retval.append(Separators.SEMICOLON);
            this.parameters.encode(retval);
        }
        return retval;
    }

    public HostPort getHostPort() {
        return this.address.getHostPort();
    }

    public String getDisplayName() {
        return this.address.getDisplayName();
    }
}
