package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.AttributeField;
import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;
import org.jitsi.gov.nist.core.NameValue;

public class AttributeFieldParser extends SDPParser {
    public AttributeFieldParser(String attributeField) {
        this.lexer = new Lexer("charLexer", attributeField);
    }

    public AttributeField attributeField() throws ParseException {
        AttributeField attributeField;
        NameValue nameValue;
        int ptr;
        try {
            attributeField = new AttributeField();
            this.lexer.match(97);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            nameValue = new NameValue();
            ptr = this.lexer.markInputPosition();
            String name = this.lexer.getNextToken(':');
            this.lexer.consume(1);
            nameValue = new NameValue(name.trim(), this.lexer.getRest().trim());
        } catch (ParseException e) {
            this.lexer.rewindInputPosition(ptr);
            String rest = this.lexer.getRest();
            if (rest == null) {
                throw new ParseException(this.lexer.getBuffer(), this.lexer.getPtr());
            }
            nameValue = new NameValue(rest.trim(), null);
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new ParseException(e2.getMessage(), 0);
        }
        attributeField.setAttribute(nameValue);
        this.lexer.SPorHT();
        return attributeField;
    }

    public SDPField parse() throws ParseException {
        return attributeField();
    }
}
