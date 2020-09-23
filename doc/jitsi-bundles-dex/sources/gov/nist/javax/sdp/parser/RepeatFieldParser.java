package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.RepeatField;
import gov.nist.javax.sdp.fields.SDPField;
import gov.nist.javax.sdp.fields.TypedTime;
import java.text.ParseException;
import org.jitsi.gov.nist.core.Token;

public class RepeatFieldParser extends SDPParser {
    public RepeatFieldParser(String repeatField) {
        this.lexer = new Lexer("charLexer", repeatField);
    }

    public TypedTime getTypedTime(String tokenValue) {
        TypedTime typedTime = new TypedTime();
        if (tokenValue.endsWith("d")) {
            typedTime.setUnit("d");
            typedTime.setTime(Integer.parseInt(tokenValue.replace('d', ' ').trim()));
        } else if (tokenValue.endsWith("h")) {
            typedTime.setUnit("h");
            typedTime.setTime(Integer.parseInt(tokenValue.replace('h', ' ').trim()));
        } else if (tokenValue.endsWith("m")) {
            typedTime.setUnit("m");
            typedTime.setTime(Integer.parseInt(tokenValue.replace('m', ' ').trim()));
        } else {
            typedTime.setUnit("s");
            if (tokenValue.endsWith("s")) {
                typedTime.setTime(Integer.parseInt(tokenValue.replace('s', ' ').trim()));
            } else {
                typedTime.setTime(Integer.parseInt(tokenValue.trim()));
            }
        }
        return typedTime;
    }

    public RepeatField repeatField() throws ParseException {
        try {
            this.lexer.match(114);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            RepeatField repeatField = new RepeatField();
            this.lexer.match(4095);
            Token repeatInterval = this.lexer.getNextToken();
            this.lexer.SPorHT();
            repeatField.setRepeatInterval(getTypedTime(repeatInterval.getTokenValue()));
            this.lexer.match(4095);
            Token activeDuration = this.lexer.getNextToken();
            this.lexer.SPorHT();
            repeatField.setActiveDuration(getTypedTime(activeDuration.getTokenValue()));
            while (this.lexer.hasMoreChars()) {
                char la = this.lexer.lookAhead(0);
                if (la == 10 || la == 13) {
                    break;
                }
                this.lexer.match(4095);
                Token offsets = this.lexer.getNextToken();
                this.lexer.SPorHT();
                repeatField.addOffset(getTypedTime(offsets.getTokenValue()));
            }
            return repeatField;
        } catch (Exception e) {
            throw this.lexer.createParseException();
        }
    }

    public SDPField parse() throws ParseException {
        return repeatField();
    }
}
