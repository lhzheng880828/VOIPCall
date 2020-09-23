package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.ServiceRoute;
import org.jitsi.gov.nist.javax.sip.header.ims.ServiceRouteList;
import org.jitsi.gov.nist.javax.sip.parser.AddressParametersParser;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class ServiceRouteParser extends AddressParametersParser {
    public ServiceRouteParser(String serviceRoute) {
        super(serviceRoute);
    }

    protected ServiceRouteParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        ServiceRouteList serviceRouteList = new ServiceRouteList();
        if (debug) {
            dbg_enter("ServiceRouteParser.parse");
        }
        try {
            this.lexer.match(TokenTypes.SERVICE_ROUTE);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            while (true) {
                ServiceRoute serviceRoute = new ServiceRoute();
                super.parse(serviceRoute);
                serviceRouteList.add((SIPHeader) serviceRoute);
                this.lexer.SPorHT();
                if (this.lexer.lookAhead(0) != ',') {
                    break;
                }
                this.lexer.match(44);
                this.lexer.SPorHT();
            }
            if (this.lexer.lookAhead(0) == 10) {
                return serviceRouteList;
            }
            throw createParseException("unexpected char");
        } finally {
            if (debug) {
                dbg_leave("ServiceRouteParser.parse");
            }
        }
    }
}
