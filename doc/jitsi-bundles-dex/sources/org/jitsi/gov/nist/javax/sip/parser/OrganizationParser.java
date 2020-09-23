package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.Organization;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;

public class OrganizationParser extends HeaderParser {
    public OrganizationParser(String organization) {
        super(organization);
    }

    protected OrganizationParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("OrganizationParser.parse");
        }
        Organization organization = new Organization();
        try {
            headerName(TokenTypes.ORGANIZATION);
            organization.setHeaderName("Organization");
            this.lexer.SPorHT();
            organization.setOrganization(this.lexer.getRest().trim());
            return organization;
        } finally {
            if (debug) {
                dbg_leave("OrganizationParser.parse");
            }
        }
    }
}
