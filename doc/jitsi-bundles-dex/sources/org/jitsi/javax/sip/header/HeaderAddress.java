package org.jitsi.javax.sip.header;

import org.jitsi.javax.sip.address.Address;

public interface HeaderAddress {
    Address getAddress();

    void setAddress(Address address);
}
