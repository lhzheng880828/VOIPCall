package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.MinExpires;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.javax.sip.InvalidArgumentException;

public class MinExpiresParser extends HeaderParser {
    public MinExpiresParser(String minExpires) {
        super(minExpires);
    }

    protected MinExpiresParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("MinExpiresParser.parse");
        }
        MinExpires minExpires = new MinExpires();
        try {
            headerName(TokenTypes.MIN_EXPIRES);
            minExpires.setHeaderName("Min-Expires");
            minExpires.setExpires(Integer.parseInt(this.lexer.number()));
            this.lexer.SPorHT();
            this.lexer.match(10);
            if (debug) {
                dbg_leave("MinExpiresParser.parse");
            }
            return minExpires;
        } catch (InvalidArgumentException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("MinExpiresParser.parse");
            }
        }
    }
}
