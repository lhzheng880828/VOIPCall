package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.header.AddressParametersHeader;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class PAssertedIdentity extends AddressParametersHeader implements PAssertedIdentityHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PAssertedIdentity(AddressImpl address) {
        super("P-Asserted-Identity");
        this.address = address;
    }

    public PAssertedIdentity() {
        super("P-Asserted-Identity");
    }

    public StringBuilder encodeBody(StringBuilder retval) {
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
        return this.parameters.encode(retval.append(Separators.COMMA));
    }

    public Object clone() {
        return (PAssertedIdentity) super.clone();
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
