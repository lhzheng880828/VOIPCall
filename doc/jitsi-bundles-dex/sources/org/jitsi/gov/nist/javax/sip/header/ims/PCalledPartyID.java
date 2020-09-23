package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.header.AddressParametersHeader;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class PCalledPartyID extends AddressParametersHeader implements PCalledPartyIDHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PCalledPartyID(AddressImpl address) {
        super("P-Called-Party-ID");
        this.address = address;
    }

    public PCalledPartyID() {
        super("P-Called-Party-ID");
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
        return this.parameters.encode(retval.append(Separators.SEMICOLON));
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
