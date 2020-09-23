package org.jitsi.gov.nist.javax.sip.header.ims;

import java.text.ParseException;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.AuthorizationHeader;

public interface AuthorizationHeaderIms extends AuthorizationHeader {
    public static final String NO = "no";
    public static final String YES = "yes";

    String getIntegrityProtected();

    void setIntegrityProtected(String str) throws InvalidArgumentException, ParseException;
}
