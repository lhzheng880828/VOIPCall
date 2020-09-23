package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.header.AddressParametersHeader;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class PPreferredIdentity extends AddressParametersHeader implements PPreferredIdentityHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PPreferredIdentity(AddressImpl address) {
        super("P-Preferred-Identity");
        this.address = address;
    }

    public PPreferredIdentity() {
        super("P-Preferred-Identity");
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        if (this.address.getAddressType() == 2) {
            retval.append(Separators.LESS_THAN);
        }
        this.address.encode(retval);
        if (this.address.getAddressType() == 2) {
            retval.append(Separators.GREATER_THAN);
        }
        return retval;
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
