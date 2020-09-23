package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.ProxyAuthorization;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;

public class ProxyAuthorizationParser extends ChallengeParser {
    public ProxyAuthorizationParser(String proxyAuthorization) {
        super(proxyAuthorization);
    }

    protected ProxyAuthorizationParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        headerName(TokenTypes.PROXY_AUTHORIZATION);
        ProxyAuthorization proxyAuth = new ProxyAuthorization();
        super.parse(proxyAuth);
        return proxyAuth;
    }
}
