package org.jitsi.gov.nist.javax.sip.parser;

import org.jitsi.gov.nist.javax.sip.stack.SIPTransactionStack;

public class StringMsgParserFactory implements MessageParserFactory {
    public MessageParser createMessageParser(SIPTransactionStack stack) {
        return new StringMsgParser();
    }
}
