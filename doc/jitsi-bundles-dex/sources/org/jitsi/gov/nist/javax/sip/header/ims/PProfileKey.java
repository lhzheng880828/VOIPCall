package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.header.AddressParametersHeader;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class PProfileKey extends AddressParametersHeader implements PProfileKeyHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PProfileKey() {
        super("P-Profile-Key");
    }

    public PProfileKey(AddressImpl address) {
        super("P-Profile-Key");
        this.address = address;
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder retval) {
        if (this.address.getAddressType() == 2) {
            retval.append(Separators.LESS_THAN);
        }
        retval.append(this.address.encode());
        if (this.address.getAddressType() == 2) {
            retval.append(Separators.GREATER_THAN);
        }
        if (!this.parameters.isEmpty()) {
            retval.append(Separators.SEMICOLON + this.parameters.encode());
        }
        return retval;
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    public boolean equals(Object other) {
        return (other instanceof PProfileKey) && super.equals(other);
    }

    public Object clone() {
        return (PProfileKey) super.clone();
    }
}
