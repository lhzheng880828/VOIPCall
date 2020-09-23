package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.SDPField;
import gov.nist.javax.sdp.fields.URIField;
import java.text.ParseException;

public class URIFieldParser extends SDPParser {
    public URIFieldParser(String uriField) {
        this.lexer = new Lexer("charLexer", uriField);
    }

    public URIField uriField() throws ParseException {
        try {
            this.lexer.match(117);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            URIField uriField = new URIField();
            uriField.setURI(this.lexer.getRest().trim());
            return uriField;
        } catch (Exception e) {
            throw this.lexer.createParseException();
        }
    }

    public SDPField parse() throws ParseException {
        return uriField();
    }
}
