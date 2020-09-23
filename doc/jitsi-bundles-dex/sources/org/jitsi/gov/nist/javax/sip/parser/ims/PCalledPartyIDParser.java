package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PCalledPartyID;
import org.jitsi.gov.nist.javax.sip.parser.AddressParametersParser;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class PCalledPartyIDParser extends AddressParametersParser {
    public PCalledPartyIDParser(String calledPartyID) {
        super(calledPartyID);
    }

    protected PCalledPartyIDParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("PCalledPartyIDParser.parse");
        }
        try {
            this.lexer.match(TokenTypes.P_CALLED_PARTY_ID);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            PCalledPartyID calledPartyID = new PCalledPartyID();
            super.parse(calledPartyID);
            return calledPartyID;
        } finally {
            if (debug) {
                dbg_leave("PCalledPartyIDParser.parse");
            }
        }
    }
}
