package gov.nist.javax.sdp.parser;

import gov.nist.javax.sdp.fields.ConnectionAddress;
import gov.nist.javax.sdp.fields.ConnectionField;
import gov.nist.javax.sdp.fields.SDPField;
import java.text.ParseException;
import org.jitsi.gov.nist.core.Host;
import org.jitsi.gov.nist.core.Separators;

public class ConnectionFieldParser extends SDPParser {
    public ConnectionFieldParser(String connectionField) {
        this.lexer = new Lexer("charLexer", connectionField);
    }

    public ConnectionAddress connectionAddress(String address) {
        ConnectionAddress connectionAddress = new ConnectionAddress();
        int begin = address.indexOf(Separators.SLASH);
        if (begin != -1) {
            connectionAddress.setAddress(new Host(address.substring(0, begin)));
            int middle = address.indexOf(Separators.SLASH, begin + 1);
            if (middle != -1) {
                connectionAddress.setTtl(Integer.parseInt(address.substring(begin + 1, middle).trim()));
                connectionAddress.setPort(Integer.parseInt(address.substring(middle + 1).trim()));
            } else {
                connectionAddress.setTtl(Integer.parseInt(address.substring(begin + 1).trim()));
            }
        } else {
            connectionAddress.setAddress(new Host(address));
        }
        return connectionAddress;
    }

    public ConnectionField connectionField() throws ParseException {
        try {
            this.lexer.match(99);
            this.lexer.SPorHT();
            this.lexer.match(61);
            this.lexer.SPorHT();
            ConnectionField connectionField = new ConnectionField();
            this.lexer.match(4095);
            this.lexer.SPorHT();
            connectionField.setNettype(this.lexer.getNextToken().getTokenValue());
            this.lexer.match(4095);
            this.lexer.SPorHT();
            connectionField.setAddressType(this.lexer.getNextToken().getTokenValue());
            this.lexer.SPorHT();
            connectionField.setAddress(connectionAddress(this.lexer.getRest().trim()));
            return connectionField;
        } catch (Exception e) {
            throw new ParseException(this.lexer.getBuffer(), this.lexer.getPtr());
        }
    }

    public SDPField parse() throws ParseException {
        return connectionField();
    }
}
