package org.jitsi.gov.nist.javax.sip.address;

import java.text.ParseException;
import org.jitsi.javax.sip.address.AddressFactory;
import org.jitsi.javax.sip.address.SipURI;

public interface AddressFactoryEx extends AddressFactory {
    SipURI createSipURI(String str) throws ParseException;
}
