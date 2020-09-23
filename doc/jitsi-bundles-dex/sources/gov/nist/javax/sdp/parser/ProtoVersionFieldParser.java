package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.ProtoVersionField;
import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;

public class ProtoVersionFieldParser extends SDPParser {
    public ProtoVersionFieldParser(String protoVersionField) {
        this.lexer = new Lexer("charLexer", protoVersionField);
    }

    public ProtoVersionField protoVersionField() throws ParseException {
        try {
            this.lexer.match(118);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            ProtoVersionField protoVersionField = new ProtoVersionField();
            this.lexer.match(4095);
            protoVersionField.setProtoVersion(Integer.parseInt(this.lexer.getNextToken().getTokenValue()));
            this.lexer.SPorHT();
            return protoVersionField;
        } catch (Exception e) {
            throw this.lexer.createParseException();
        }
    }

    public SDPField parse() throws ParseException {
        return protoVersionField();
    }
}
