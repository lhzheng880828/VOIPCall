package org.jitsi.gov.nist.javax.sip.header.extensions;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.AddressParametersHeader;
import org.jitsi.javax.sip.header.ExtensionHeader;

public final class ReferredBy extends AddressParametersHeader implements ExtensionHeader, ReferredByHeader {
    public static final String NAME = "Referred-By";
    private static final long serialVersionUID = 3134344915465784267L;

    public ReferredBy() {
        super("Referred-By");
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
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
