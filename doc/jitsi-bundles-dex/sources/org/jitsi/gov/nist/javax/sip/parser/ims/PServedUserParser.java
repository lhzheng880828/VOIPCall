package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.address.AddressFactoryImpl;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PServedUser;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.ParametersParser;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class PServedUserParser extends ParametersParser implements TokenTypes {
    protected PServedUserParser(Lexer lexer) {
        super(lexer);
    }

    public PServedUserParser(String servedUser) {
        super(servedUser);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("PServedUser.parse");
        }
        try {
            this.lexer.match(TokenTypes.P_SERVED_USER);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            PServedUser servedUser = new PServedUser();
            this.lexer.SPorHT();
            servedUser.setAddress(new AddressFactoryImpl().createAddress(this.lexer.byteStringNoSemicolon()));
            super.parse(servedUser);
            return servedUser;
        } finally {
            if (debug) {
                dbg_leave("PServedUser.parse");
            }
        }
    }
}
