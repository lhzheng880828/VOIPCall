package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class SecurityServer extends SecurityAgree implements SecurityServerHeader, ExtensionHeader {
    public SecurityServer() {
        super("Security-Server");
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
