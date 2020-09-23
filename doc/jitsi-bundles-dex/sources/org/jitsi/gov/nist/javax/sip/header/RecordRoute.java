package org.jitsi.gov.nist.javax.sip.header;

import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.javax.sip.header.RecordRouteHeader;

public class RecordRoute extends AddressParametersHeader implements RecordRouteHeader {
    private static final long serialVersionUID = 2388023364181727205L;

    public RecordRoute(AddressImpl address) {
        super("Record-Route");
        this.address = address;
    }

    public RecordRoute() {
        super("Record-Route");
    }

    public String encodeBody() {
        return encodeBody(new StringBuilder()).toString();
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder buffer) {
        if (this.address.getAddressType() == 2) {
            buffer.append(Separators.LESS_THAN);
        }
        this.address.encode(buffer);
        if (this.address.getAddressType() == 2) {
            buffer.append(Separators.GREATER_THAN);
        }
        if (!this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }
}
