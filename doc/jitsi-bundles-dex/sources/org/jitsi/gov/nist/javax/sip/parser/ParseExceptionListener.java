package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;

public interface ParseExceptionListener {
    void handleException(ParseException parseException, SIPMessage sIPMessage, Class cls, String str, String str2) throws ParseException;
}
