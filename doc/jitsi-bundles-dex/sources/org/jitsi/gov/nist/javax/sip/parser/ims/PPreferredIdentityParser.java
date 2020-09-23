package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PPreferredIdentity;
import org.jitsi.gov.nist.javax.sip.parser.AddressParametersParser;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class PPreferredIdentityParser extends AddressParametersParser implements TokenTypes {
    public PPreferredIdentityParser(String preferredIdentity) {
        super(preferredIdentity);
    }

    protected PPreferredIdentityParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("PreferredIdentityParser.parse");
        }
        try {
            this.lexer.match(TokenTypes.P_PREFERRED_IDENTITY);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            PPreferredIdentity p = new PPreferredIdentity();
            super.parse(p);
            return p;
        } finally {
            if (debug) {
                dbg_leave("PreferredIdentityParser.parse");
            }
        }
    }
}
