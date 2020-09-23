package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.message.SIPMessage;

public interface MessageParser {
    SIPMessage parseSIPMessage(byte[] bArr, boolean z, boolean z2, ParseExceptionListener parseExceptionListener) throws ParseException;
}
