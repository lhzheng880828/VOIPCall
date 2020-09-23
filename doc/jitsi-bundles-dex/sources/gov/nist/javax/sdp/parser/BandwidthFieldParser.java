package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.BandwidthField;
import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;
import org.jitsi.gov.nist.core.NameValue;

public class BandwidthFieldParser extends SDPParser {
    public BandwidthFieldParser(String bandwidthField) {
        this.lexer = new Lexer("charLexer", bandwidthField);
    }

    public BandwidthField bandwidthField() throws ParseException {
        try {
            this.lexer.match(98);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            BandwidthField bandwidthField = new BandwidthField();
            NameValue nameValue = nameValue(':');
            String name = nameValue.getName();
            bandwidthField.setBandwidth(Integer.parseInt(((String) nameValue.getValueAsObject()).trim()));
            bandwidthField.setBwtype(name);
            this.lexer.SPorHT();
            return bandwidthField;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParseException(this.lexer.getBuffer(), this.lexer.getPtr());
        }
    }

    public SDPField parse() throws ParseException {
        return bandwidthField();
    }
}
