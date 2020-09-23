package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.PhoneField;
import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;

public class PhoneFieldParser extends SDPParser {
    public PhoneFieldParser(String phoneField) {
        this.lexer = new Lexer("charLexer", phoneField);
    }

    public String getDisplayName(String rest) {
        try {
            int begin = rest.indexOf(Separators.LPAREN);
            int end = rest.indexOf(Separators.RPAREN);
            if (begin != -1) {
                return rest.substring(begin + 1, end);
            }
            int ind = rest.indexOf(Separators.LESS_THAN);
            if (ind != -1) {
                return rest.substring(0, ind);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getPhoneNumber(String rest) throws ParseException {
        try {
            int begin = rest.indexOf(Separators.LPAREN);
            if (begin != -1) {
                return rest.substring(0, begin).trim();
            }
            int ind = rest.indexOf(Separators.LESS_THAN);
            int end = rest.indexOf(Separators.GREATER_THAN);
            if (ind != -1) {
                return rest.substring(ind + 1, end);
            }
            return rest.trim();
        } catch (Exception e) {
            throw new ParseException(this.lexer.getBuffer(), this.lexer.getPtr());
        }
    }

    public PhoneField phoneField() throws ParseException {
        try {
            this.lexer.match(112);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            PhoneField phoneField = new PhoneField();
            String rest = this.lexer.getRest();
            phoneField.setName(getDisplayName(rest.trim()));
            phoneField.setPhoneNumber(getPhoneNumber(rest));
            return phoneField;
        } catch (Exception e) {
            throw new ParseException(this.lexer.getBuffer(), this.lexer.getPtr());
        }
    }

    public SDPField parse() throws ParseException {
        return phoneField();
    }
}
