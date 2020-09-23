package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.SDPField;
import gov.nist.javax.sdp.fields.SessionNameField;
import java.text.ParseException;

public class SessionNameFieldParser extends SDPParser {
    public SessionNameFieldParser(String sessionNameField) {
        this.lexer = new Lexer("charLexer", sessionNameField);
    }

    public SessionNameField sessionNameField() throws ParseException {
        try {
            this.lexer.match(115);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            SessionNameField sessionNameField = new SessionNameField();
            String rest = this.lexer.getRest();
            sessionNameField.setSessionName(rest == null ? "" : rest.trim());
            return sessionNameField;
        } catch (Exception e) {
            throw this.lexer.createParseException();
        }
    }

    public SDPField parse() throws ParseException {
        return sessionNameField();
    }

    public static void main(String[] args) throws ParseException {
        String[] session = new String[]{"s=SDP Seminar \n", "s= Session SDP\n"};
        for (String sessionNameFieldParser : session) {
            System.out.println("encoded: " + new SessionNameFieldParser(sessionNameFieldParser).sessionNameField().encode());
        }
    }
}
