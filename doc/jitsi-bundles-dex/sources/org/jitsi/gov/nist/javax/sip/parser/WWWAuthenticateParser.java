package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.WWWAuthenticate;

public class WWWAuthenticateParser extends ChallengeParser {
    public WWWAuthenticateParser(String wwwAuthenticate) {
        super(wwwAuthenticate);
    }

    protected WWWAuthenticateParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("parse");
        }
        try {
            headerName(TokenTypes.WWW_AUTHENTICATE);
            WWWAuthenticate wwwAuthenticate = new WWWAuthenticate();
            super.parse(wwwAuthenticate);
            return wwwAuthenticate;
        } finally {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }
}
