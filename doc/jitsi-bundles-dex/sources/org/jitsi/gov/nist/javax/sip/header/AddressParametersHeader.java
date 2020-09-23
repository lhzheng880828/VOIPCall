package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.HeaderAddress;
import org.jitsi.javax.sip.header.Parameters;

public abstract class AddressParametersHeader extends ParametersHeader implements Parameters {
    protected AddressImpl address;

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address address) {
        this.address = (AddressImpl) address;
    }

    protected AddressParametersHeader(String name) {
        super(name);
    }

    protected AddressParametersHeader(String name, boolean sync) {
        super(name, sync);
    }

    public Object clone() {
        AddressParametersHeader retval = (AddressParametersHeader) super.clone();
        if (this.address != null) {
            retval.address = (AddressImpl) this.address.clone();
        }
        return retval;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HeaderAddress) || !(other instanceof Parameters)) {
            return false;
        }
        HeaderAddress o = (HeaderAddress) other;
        if (getAddress().equals(o.getAddress()) && equalParameters((Parameters) o)) {
            return true;
        }
        return false;
    }
}
