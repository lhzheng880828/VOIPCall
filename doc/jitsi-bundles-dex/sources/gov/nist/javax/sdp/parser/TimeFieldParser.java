package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.SDPField;
import gov.nist.javax.sdp.fields.TimeField;
import gov.nist.javax.sdp.fields.TypedTime;
import java.text.ParseException;

public class TimeFieldParser extends SDPParser {
    public TimeFieldParser(String timeField) {
        this.lexer = new Lexer("charLexer", timeField);
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

    private long getTime() throws ParseException {
        try {
            String startTime = this.lexer.number();
            if (startTime.length() > 18) {
                startTime = startTime.substring(startTime.length() - 18);
            }
            return Long.parseLong(startTime);
        } catch (NumberFormatException e) {
            throw this.lexer.createParseException();
        }
    }

    public TimeField timeField() throws ParseException {
        try {
            this.lexer.match(116);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            TimeField timeField = new TimeField();
            timeField.setStartTime(getTime());
            this.lexer.SPorHT();
            timeField.setStopTime(getTime());
            return timeField;
        } catch (Exception e) {
            throw this.lexer.createParseException();
        }
    }

    public SDPField parse() throws ParseException {
        return timeField();
    }
}
