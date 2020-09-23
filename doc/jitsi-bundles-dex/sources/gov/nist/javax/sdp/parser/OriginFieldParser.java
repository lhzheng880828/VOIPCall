package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.OriginField;
import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;
import org.jitsi.gov.nist.core.HostNameParser;
import org.jitsi.gov.nist.core.LexerCore;

public class OriginFieldParser extends SDPParser {
    public OriginFieldParser(String originField) {
        this.lexer = new Lexer("charLexer", originField);
    }

    public OriginField originField() throws ParseException {
        try {
            OriginField originField = new OriginField();
            this.lexer.match(111);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            this.lexer.match(LexerCore.SAFE);
            originField.setUsername(this.lexer.getNextToken().getTokenValue());
            this.lexer.SPorHT();
            this.lexer.match(4095);
            String sessId = this.lexer.getNextToken().getTokenValue();
            if (sessId.length() > 18) {
                sessId = sessId.substring(sessId.length() - 18);
            }
            try {
                originField.setSessId(Long.parseLong(sessId));
            } catch (NumberFormatException e) {
                originField.setSessionId(sessId);
            }
            this.lexer.SPorHT();
            this.lexer.match(4095);
            String sessVer = this.lexer.getNextToken().getTokenValue();
            if (sessVer.length() > 18) {
                sessVer = sessVer.substring(sessVer.length() - 18);
            }
            try {
                originField.setSessVersion(Long.parseLong(sessVer));
            } catch (NumberFormatException e2) {
                originField.setSessVersion(sessVer);
            }
            this.lexer.SPorHT();
            this.lexer.match(4095);
            originField.setNettype(this.lexer.getNextToken().getTokenValue());
            this.lexer.SPorHT();
            this.lexer.match(4095);
            originField.setAddrtype(this.lexer.getNextToken().getTokenValue());
            this.lexer.SPorHT();
            originField.setAddress(new HostNameParser(this.lexer.getRest()).host());
            return originField;
        } catch (Exception e3) {
            e3.printStackTrace();
            throw new ParseException(this.lexer.getBuffer(), this.lexer.getPtr());
        }
    }

    public SDPField parse() throws ParseException {
        return originField();
    }

    public static void main(String[] args) throws ParseException {
        String[] origin = new String[]{"o=- 45ec4ba1.1 45ec4ba1 in ip4 10.1.80.200\r\n", "o=- 4322650003578 0 IN IP4 192.53.18.122\r\n", "o=4855 12345678901234567890 12345678901234567890 IN IP4 166.35.224.216\n", "o=mh/andley 2890844526 2890842807 IN IP4 126.16.64.4\n", "o=UserB 2890844527 2890844527 IN IP4 everywhere.com\n", "o=UserA 2890844526 2890844526 IN IP4 here.com\n", "o=IFAXTERMINAL01 2890844527 2890844527 IN IP4 ift.here.com\n", "o=GATEWAY1 2890844527 2890844527 IN IP4 gatewayone.wcom.com\n", "o=- 2890844527 2890844527 IN IP4 gatewayone.wcom.com\n"};
        for (int i = 0; i < origin.length; i++) {
            OriginField originField = new OriginFieldParser(origin[i]).originField();
            System.out.println("toParse :" + origin[i]);
            System.out.println("encoded: " + originField.encode());
        }
    }
}
