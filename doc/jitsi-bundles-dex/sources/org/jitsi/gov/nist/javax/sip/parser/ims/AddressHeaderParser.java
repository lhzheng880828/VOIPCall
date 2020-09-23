package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.ims.AddressHeaderIms;
import org.jitsi.gov.nist.javax.sip.parser.AddressParser;
import org.jitsi.gov.nist.javax.sip.parser.HeaderParser;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;

abstract class AddressHeaderParser extends HeaderParser {
    protected AddressHeaderParser(Lexer lexer) {
        super(lexer);
    }

    protected AddressHeaderParser(String buffer) {
        super(buffer);
    }

    /* access modifiers changed from: protected */
    public void parse(AddressHeaderIms addressHeader) throws ParseException {
        dbg_enter("AddressHeaderParser.parse");
        try {
            addressHeader.setAddress(new AddressParser(getLexer()).address(true));
            dbg_leave("AddressParametersParser.parse");
        } catch (ParseException ex) {
            throw ex;
        } catch (Throwable th) {
            dbg_leave("AddressParametersParser.parse");
        }
    }
}
