package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Debug;
import org.jitsi.gov.nist.javax.sip.header.CSeq;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.message.SIPRequest;
import org.jitsi.javax.sip.InvalidArgumentException;

public class CSeqParser extends HeaderParser {
    public CSeqParser(String cseq) {
        super(cseq);
    }

    protected CSeqParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        try {
            CSeq c = new CSeq();
            this.lexer.match(TokenTypes.CSEQ);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            c.setSeqNumber(Long.parseLong(this.lexer.number()));
            this.lexer.SPorHT();
            c.setMethod(SIPRequest.getCannonicalName(method()).intern());
            this.lexer.SPorHT();
            this.lexer.match(10);
            return c;
        } catch (NumberFormatException ex) {
            Debug.printStackTrace(ex);
            throw createParseException("Number format exception");
        } catch (InvalidArgumentException ex2) {
            Debug.printStackTrace(ex2);
            throw createParseException(ex2.getMessage());
        }
    }
}
