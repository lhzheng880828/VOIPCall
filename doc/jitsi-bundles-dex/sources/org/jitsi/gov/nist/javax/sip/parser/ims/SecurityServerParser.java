package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityServer;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityServerList;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class SecurityServerParser extends SecurityAgreeParser {
    public SecurityServerParser(String security) {
        super(security);
    }

    protected SecurityServerParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        dbg_enter("SecuriryServer parse");
        try {
            headerName(TokenTypes.SECURITY_SERVER);
            SecurityServerList secServerList = (SecurityServerList) super.parse(new SecurityServer());
            return secServerList;
        } finally {
            dbg_leave("SecuriryServer parse");
        }
    }
}
