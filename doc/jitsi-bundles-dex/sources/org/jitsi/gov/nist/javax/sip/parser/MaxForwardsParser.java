package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.MaxForwards;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.javax.sip.InvalidArgumentException;

public class MaxForwardsParser extends HeaderParser {
    public MaxForwardsParser(String contentLength) {
        super(contentLength);
    }

    protected MaxForwardsParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("MaxForwardsParser.enter");
        }
        try {
            MaxForwards contentLength = new MaxForwards();
            headerName(TokenTypes.MAX_FORWARDS);
            contentLength.setMaxForwards(Integer.parseInt(this.lexer.number()));
            this.lexer.SPorHT();
            this.lexer.match(10);
            if (debug) {
                dbg_leave("MaxForwardsParser.leave");
            }
            return contentLength;
        } catch (InvalidArgumentException ex) {
            throw createParseException(ex.getMessage());
        } catch (NumberFormatException ex2) {
            throw createParseException(ex2.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("MaxForwardsParser.leave");
            }
        }
    }
}
