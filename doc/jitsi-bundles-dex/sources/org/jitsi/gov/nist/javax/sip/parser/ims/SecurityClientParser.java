package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityClient;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityClientList;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class SecurityClientParser extends SecurityAgreeParser {
    public SecurityClientParser(String security) {
        super(security);
    }

    protected SecurityClientParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        dbg_enter("SecuriryClient parse");
        try {
            headerName(TokenTypes.SECURITY_CLIENT);
            SecurityClientList secClientList = (SecurityClientList) super.parse(new SecurityClient());
            return secClientList;
        } finally {
            dbg_leave("SecuriryClient parse");
        }
    }
}
