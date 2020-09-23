package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.address.GenericURI;
import org.jitsi.gov.nist.javax.sip.header.AddressParametersHeader;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class PAssociatedURI extends AddressParametersHeader implements PAssociatedURIHeader, SIPHeaderNamesIms, ExtensionHeader {
    public PAssociatedURI() {
        super("P-Associated-URI");
    }

    public PAssociatedURI(AddressImpl address) {
        super("P-Associated-URI");
        this.address = address;
    }

    public PAssociatedURI(GenericURI associatedURI) {
        super("P-Associated-URI");
        this.address = new AddressImpl();
        this.address.setURI(associatedURI);
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

    public void setAssociatedURI(URI associatedURI) throws NullPointerException {
        if (associatedURI == null) {
            throw new NullPointerException("null URI");
        }
        this.address.setURI(associatedURI);
    }

    public URI getAssociatedURI() {
        return this.address.getURI();
    }

    public Object clone() {
        PAssociatedURI retval = (PAssociatedURI) super.clone();
        if (this.address != null) {
            retval.address = (AddressImpl) this.address.clone();
        }
        return retval;
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
