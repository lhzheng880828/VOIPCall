package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PAssociatedURI;
import org.jitsi.gov.nist.javax.sip.header.ims.PAssociatedURIList;
import org.jitsi.gov.nist.javax.sip.parser.AddressParametersParser;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class PAssociatedURIParser extends AddressParametersParser {
    public PAssociatedURIParser(String associatedURI) {
        super(associatedURI);
    }

    protected PAssociatedURIParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("PAssociatedURIParser.parse");
        }
        PAssociatedURIList associatedURIList = new PAssociatedURIList();
        try {
            headerName(TokenTypes.P_ASSOCIATED_URI);
            PAssociatedURI associatedURI = new PAssociatedURI();
            associatedURI.setHeaderName("P-Associated-URI");
            super.parse(associatedURI);
            associatedURIList.add((SIPHeader) associatedURI);
            this.lexer.SPorHT();
            while (this.lexer.lookAhead(0) == ',') {
                this.lexer.match(44);
                this.lexer.SPorHT();
                associatedURI = new PAssociatedURI();
                super.parse(associatedURI);
                associatedURIList.add((SIPHeader) associatedURI);
                this.lexer.SPorHT();
            }
            this.lexer.SPorHT();
            this.lexer.match(10);
            return associatedURIList;
        } finally {
            if (debug) {
                dbg_leave("PAssociatedURIParser.parse");
            }
        }
    }
}
