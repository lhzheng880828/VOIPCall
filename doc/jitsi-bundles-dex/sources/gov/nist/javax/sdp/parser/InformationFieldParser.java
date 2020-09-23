package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.InformationField;
import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;

public class InformationFieldParser extends SDPParser {
    public InformationFieldParser(String informationField) {
        this.lexer = new Lexer("charLexer", informationField);
    }

    public InformationField informationField() throws ParseException {
        try {
            this.lexer.match(105);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            InformationField informationField = new InformationField();
            informationField.setInformation(this.lexer.getRest().trim());
            return informationField;
        } catch (Exception e) {
            throw new ParseException(this.lexer.getBuffer(), this.lexer.getPtr());
        }
    }

    public SDPField parse() throws ParseException {
        return informationField();
    }
}
