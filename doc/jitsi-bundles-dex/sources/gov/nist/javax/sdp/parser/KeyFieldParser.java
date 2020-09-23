package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.KeyField;
import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;
import org.jitsi.gov.nist.core.NameValue;

public class KeyFieldParser extends SDPParser {
    public KeyFieldParser(String keyField) {
        this.lexer = new Lexer("charLexer", keyField);
    }

    public KeyField keyField() throws ParseException {
        KeyField keyField;
        NameValue nameValue;
        int ptr;
        try {
            this.lexer.match(107);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            keyField = new KeyField();
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
            throw new ParseException(this.lexer.getBuffer(), this.lexer.getPtr());
        }
        keyField.setType(nameValue.getName());
        keyField.setKeyData((String) nameValue.getValueAsObject());
        this.lexer.SPorHT();
        return keyField;
    }

    public SDPField parse() throws ParseException {
        return keyField();
    }
}
