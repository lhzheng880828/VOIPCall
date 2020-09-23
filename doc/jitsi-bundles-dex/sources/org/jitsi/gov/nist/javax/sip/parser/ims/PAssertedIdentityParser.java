package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PAssertedIdentity;
import org.jitsi.gov.nist.javax.sip.header.ims.PAssertedIdentityList;
import org.jitsi.gov.nist.javax.sip.parser.AddressParametersParser;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class PAssertedIdentityParser extends AddressParametersParser implements TokenTypes {
    public PAssertedIdentityParser(String assertedIdentity) {
        super(assertedIdentity);
    }

    protected PAssertedIdentityParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("AssertedIdentityParser.parse");
        }
        PAssertedIdentityList assertedIdList = new PAssertedIdentityList();
        try {
            headerName(TokenTypes.P_ASSERTED_IDENTITY);
            PAssertedIdentity pai = new PAssertedIdentity();
            pai.setHeaderName("P-Asserted-Identity");
            super.parse(pai);
            assertedIdList.add((SIPHeader) pai);
            this.lexer.SPorHT();
            while (this.lexer.lookAhead(0) == ',') {
                this.lexer.match(44);
                this.lexer.SPorHT();
                pai = new PAssertedIdentity();
                super.parse(pai);
                assertedIdList.add((SIPHeader) pai);
                this.lexer.SPorHT();
            }
            this.lexer.SPorHT();
            this.lexer.match(10);
            return assertedIdList;
        } finally {
            if (debug) {
                dbg_leave("AssertedIdentityParser.parse");
            }
        }
    }
}
