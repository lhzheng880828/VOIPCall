package org.jitsi.gov.nist.javax.sip.parser.extensions;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.extensions.References;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.ParametersParser;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class ReferencesParser extends ParametersParser {
    public ReferencesParser(String references) {
        super(references);
    }

    protected ReferencesParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("ReasonParser.parse");
        }
        try {
            headerName(TokenTypes.REFERENCES);
            References references = new References();
            this.lexer.SPorHT();
            references.setCallId(this.lexer.byteStringNoSemicolon());
            super.parse(references);
            return references;
        } finally {
            if (debug) {
                dbg_leave("ReferencesParser.parse");
            }
        }
    }
}
