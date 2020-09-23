package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.header.ReferToHeader;

public final class ReferTo extends AddressParametersHeader implements ReferToHeader {
    private static final long serialVersionUID = -1666700428440034851L;

    public ReferTo() {
        super(ReferToHeader.NAME);
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        if (this.address == null) {
            return null;
        }
        if (this.address.getAddressType() == 2) {
            retval.append(Separators.LESS_THAN);
        }
        this.address.encode(retval);
        if (this.address.getAddressType() == 2) {
            retval.append(Separators.GREATER_THAN);
        }
        if (this.parameters.isEmpty()) {
            return retval;
        }
        retval.append(Separators.SEMICOLON);
        this.parameters.encode(retval);
        return retval;
    }
}
