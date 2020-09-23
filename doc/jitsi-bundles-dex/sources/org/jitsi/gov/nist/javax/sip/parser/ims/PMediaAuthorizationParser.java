package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PMediaAuthorization;
import org.jitsi.gov.nist.javax.sip.header.ims.PMediaAuthorizationList;
import org.jitsi.gov.nist.javax.sip.parser.HeaderParser;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;
import org.jitsi.javax.sip.InvalidArgumentException;

public class PMediaAuthorizationParser extends HeaderParser implements TokenTypes {
    public PMediaAuthorizationParser(String mediaAuthorization) {
        super(mediaAuthorization);
    }

    public PMediaAuthorizationParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        PMediaAuthorizationList mediaAuthorizationList = new PMediaAuthorizationList();
        if (debug) {
            dbg_enter("MediaAuthorizationParser.parse");
        }
        try {
            headerName(TokenTypes.P_MEDIA_AUTHORIZATION);
            PMediaAuthorization mediaAuthorization = new PMediaAuthorization();
            mediaAuthorization.setHeaderName("P-Media-Authorization");
            while (this.lexer.lookAhead(0) != 10) {
                this.lexer.match(4095);
                mediaAuthorization.setMediaAuthorizationToken(this.lexer.getNextToken().getTokenValue());
                mediaAuthorizationList.add((SIPHeader) mediaAuthorization);
                this.lexer.SPorHT();
                if (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    mediaAuthorization = new PMediaAuthorization();
                }
                this.lexer.SPorHT();
            }
            if (debug) {
                dbg_leave("MediaAuthorizationParser.parse");
            }
            return mediaAuthorizationList;
        } catch (InvalidArgumentException e) {
            throw createParseException(e.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("MediaAuthorizationParser.parse");
            }
        }
    }
}
