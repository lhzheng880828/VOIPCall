package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityVerify;
import org.jitsi.gov.nist.javax.sip.header.ims.SecurityVerifyList;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class SecurityVerifyParser extends SecurityAgreeParser {
    public SecurityVerifyParser(String security) {
        super(security);
    }

    protected SecurityVerifyParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        dbg_enter("SecuriryVerify parse");
        try {
            headerName(TokenTypes.SECURITY_VERIFY);
            SecurityVerifyList secVerifyList = (SecurityVerifyList) super.parse(new SecurityVerify());
            return secVerifyList;
        } finally {
            dbg_leave("SecuriryVerify parse");
        }
    }
}
