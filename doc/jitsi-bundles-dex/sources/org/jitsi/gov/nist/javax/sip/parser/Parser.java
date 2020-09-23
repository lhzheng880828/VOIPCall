package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Debug;
import org.jitsi.gov.nist.core.LexerCore;
import org.jitsi.gov.nist.core.ParserCore;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.Token;
import org.jitsi.gov.nist.javax.sip.SIPConstants;

public abstract class Parser extends ParserCore implements TokenTypes {
    /* access modifiers changed from: protected */
    public ParseException createParseException(String exceptionString) {
        return new ParseException(this.lexer.getBuffer() + Separators.COLON + exceptionString, this.lexer.getPtr());
    }

    /* access modifiers changed from: protected */
    public Lexer getLexer() {
        return (Lexer) this.lexer;
    }

    /* access modifiers changed from: protected */
    public String sipVersion() throws ParseException {
        if (debug) {
            dbg_enter("sipVersion");
        }
        try {
            if (!this.lexer.match(TokenTypes.SIP).getTokenValue().equalsIgnoreCase("SIP")) {
                createParseException("Expecting SIP");
            }
            this.lexer.match(47);
            if (!this.lexer.match(4095).getTokenValue().equals("2.0")) {
                createParseException("Expecting SIP/2.0");
            }
            String str = SIPConstants.SIP_VERSION_STRING;
            return str;
        } finally {
            if (debug) {
                dbg_leave("sipVersion");
            }
        }
    }

    /* access modifiers changed from: protected */
    public String method() throws ParseException {
        try {
            if (debug) {
                dbg_enter("method");
            }
            Token token = this.lexer.peekNextToken(1)[0];
            if (token.getTokenType() == TokenTypes.INVITE || token.getTokenType() == TokenTypes.ACK || token.getTokenType() == TokenTypes.OPTIONS || token.getTokenType() == TokenTypes.BYE || token.getTokenType() == TokenTypes.REGISTER || token.getTokenType() == TokenTypes.CANCEL || token.getTokenType() == TokenTypes.SUBSCRIBE || token.getTokenType() == TokenTypes.NOTIFY || token.getTokenType() == TokenTypes.PUBLISH || token.getTokenType() == TokenTypes.MESSAGE || token.getTokenType() == 4095) {
                this.lexer.consume();
                String tokenValue = token.getTokenValue();
                return tokenValue;
            }
            throw createParseException("Invalid Method");
        } finally {
            if (Debug.debug) {
                dbg_leave("method");
            }
        }
    }

    public static final void checkToken(String token) throws ParseException {
        if (token == null || token.length() == 0) {
            throw new ParseException("null or empty token", -1);
        }
        int i = 0;
        while (i < token.length()) {
            if (LexerCore.isTokenChar(token.charAt(i))) {
                i++;
            } else {
                throw new ParseException("Invalid character(s) in string (not allowed in 'token')", i);
            }
        }
    }
}
