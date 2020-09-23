package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.To;

public class ToParser extends AddressParametersParser {
    public ToParser(String to) {
        super(to);
    }

    protected ToParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        headerName(TokenTypes.TO);
        To to = new To();
        super.parse(to);
        this.lexer.match(10);
        return to;
    }
}
