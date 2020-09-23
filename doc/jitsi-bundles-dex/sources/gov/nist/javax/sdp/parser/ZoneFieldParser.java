package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.SDPField;
import gov.nist.javax.sdp.fields.TypedTime;
import gov.nist.javax.sdp.fields.ZoneAdjustment;
import gov.nist.javax.sdp.fields.ZoneField;
import java.text.ParseException;
import org.jitsi.gov.nist.core.Token;

public class ZoneFieldParser extends SDPParser {
    public ZoneFieldParser(String zoneField) {
        this.lexer = new Lexer("charLexer", zoneField);
    }

    public String getSign(String tokenValue) {
        if (tokenValue.startsWith("-")) {
            return "-";
        }
        return "+";
    }

    public TypedTime getTypedTime(String tokenValue) {
        String offset;
        TypedTime typedTime = new TypedTime();
        if (tokenValue.startsWith("-")) {
            offset = tokenValue.replace('-', ' ');
        } else if (tokenValue.startsWith("+")) {
            offset = tokenValue.replace('+', ' ');
        } else {
            offset = tokenValue;
        }
        if (offset.endsWith("d")) {
            typedTime.setUnit("d");
            typedTime.setTime(Integer.parseInt(offset.replace('d', ' ').trim()));
        } else if (offset.endsWith("h")) {
            typedTime.setUnit("h");
            typedTime.setTime(Integer.parseInt(offset.replace('h', ' ').trim()));
        } else if (offset.endsWith("m")) {
            typedTime.setUnit("m");
            typedTime.setTime(Integer.parseInt(offset.replace('m', ' ').trim()));
        } else {
            typedTime.setUnit("s");
            if (offset.endsWith("s")) {
                typedTime.setTime(Integer.parseInt(offset.replace('s', ' ').trim()));
            } else {
                typedTime.setTime(Integer.parseInt(offset.trim()));
            }
        }
        return typedTime;
    }

    public ZoneField zoneField() throws ParseException {
        try {
            ZoneField zoneField = new ZoneField();
            this.lexer.match(122);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            while (this.lexer.hasMoreChars()) {
                char la = this.lexer.lookAhead(0);
                if (la == 10 || la == 13) {
                    break;
                }
                ZoneAdjustment zoneAdjustment = new ZoneAdjustment();
                this.lexer.match(4095);
                Token time = this.lexer.getNextToken();
                this.lexer.SPorHT();
                String timeValue = time.getTokenValue();
                if (timeValue.length() > 18) {
                    timeValue = timeValue.substring(timeValue.length() - 18);
                }
                zoneAdjustment.setTime(Long.parseLong(timeValue));
                this.lexer.match(4095);
                Token offset = this.lexer.getNextToken();
                this.lexer.SPorHT();
                String sign = getSign(offset.getTokenValue());
                TypedTime typedTime = getTypedTime(offset.getTokenValue());
                zoneAdjustment.setSign(sign);
                zoneAdjustment.setOffset(typedTime);
                zoneField.addZoneAdjustment(zoneAdjustment);
            }
            return zoneField;
        } catch (Exception e) {
            throw this.lexer.createParseException();
        }
    }

    public SDPField parse() throws ParseException {
        return zoneField();
    }
}
