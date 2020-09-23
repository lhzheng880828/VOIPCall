package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.CallID;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;

public class CallIDParser extends HeaderParser {
    public CallIDParser(String callID) {
        super(callID);
    }

    protected CallIDParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("parse");
        }
        try {
            this.lexer.match(TokenTypes.CALL_ID);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            CallID callID = new CallID();
            this.lexer.SPorHT();
            callID.setCallId(this.lexer.getRest().trim());
            return callID;
        } finally {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }
}
