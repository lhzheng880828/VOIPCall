package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.javax.sip.header.Header;

public interface PrivacyHeader extends Header {
    public static final String NAME = "Privacy";

    String getPrivacy();

    void setPrivacy(String str) throws ParseException;
}
