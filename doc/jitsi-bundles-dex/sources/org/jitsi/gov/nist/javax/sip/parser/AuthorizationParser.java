package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.Authorization;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;

public class AuthorizationParser extends ChallengeParser {
    public AuthorizationParser(String authorization) {
        super(authorization);
    }

    protected AuthorizationParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        dbg_enter("parse");
        try {
            headerName(TokenTypes.AUTHORIZATION);
            Authorization auth = new Authorization();
            super.parse(auth);
            return auth;
        } finally {
            dbg_leave("parse");
        }
    }
}
