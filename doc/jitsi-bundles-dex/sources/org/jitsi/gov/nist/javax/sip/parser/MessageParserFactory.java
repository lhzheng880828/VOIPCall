package org.jitsi.gov.nist.javax.sip.parser;

import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack;

public interface MessageParserFactory {
    MessageParser createMessageParser(SIPTransactionStack sIPTransactionStack);
}
