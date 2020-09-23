package org.jitsi.gov.nist.javax.sip.header;

import java.util.Map;
import java.util.Map.Entry;
import org.jitsi.javax.sip.address.Address;
import org.jitsi.javax.sip.header.Parameters;

public interface AddressParameters extends Parameters {
    Address getAddress();

    Map<String, Entry<String, String>> getParameters();

    void setAddress(Address address);
}
