package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.RSeq;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.javax.sip.InvalidArgumentException;

public class RSeqParser extends HeaderParser {
    public RSeqParser(String rseq) {
        super(rseq);
    }

    protected RSeqParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("RSeqParser.parse");
        }
        RSeq rseq = new RSeq();
        try {
            headerName(TokenTypes.RSEQ);
            rseq.setHeaderName("RSeq");
            rseq.setSeqNumber(Long.parseLong(this.lexer.number()));
            this.lexer.SPorHT();
            this.lexer.match(10);
            if (debug) {
                dbg_leave("RSeqParser.parse");
            }
            return rseq;
        } catch (InvalidArgumentException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("RSeqParser.parse");
            }
        }
    }
}
