package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class SecurityVerify extends SecurityAgree implements SecurityVerifyHeader, ExtensionHeader {
    public SecurityVerify() {
        super("Security-Verify");
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }
}
