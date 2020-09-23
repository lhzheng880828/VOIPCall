package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PProfileKey;
import org.jitsi.gov.nist.javax.sip.parser.AddressParametersParser;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class PProfileKeyParser extends AddressParametersParser implements TokenTypes {
    protected PProfileKeyParser(Lexer lexer) {
        super(lexer);
    }

    public PProfileKeyParser(String profilekey) {
        super(profilekey);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("PProfileKey.parse");
        }
        try {
            this.lexer.match(TokenTypes.P_PROFILE_KEY);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            PProfileKey p = new PProfileKey();
            super.parse(p);
            return p;
        } finally {
            if (debug) {
                dbg_leave("PProfileKey.parse");
            }
        }
    }
}
