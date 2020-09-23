package org.jitsi.gov.nist.javax.sip.parser.ims;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.ims.PVisitedNetworkID;
import org.jitsi.gov.nist.javax.sip.header.ims.PVisitedNetworkIDList;
import org.jitsi.gov.nist.javax.sip.parser.Lexer;
import org.jitsi.gov.nist.javax.sip.parser.ParametersParser;
import org.jitsi.gov.nist.javax.sip.parser.TokenTypes;

public class PVisitedNetworkIDParser extends ParametersParser implements TokenTypes {
    public PVisitedNetworkIDParser(String networkID) {
        super(networkID);
    }

    protected PVisitedNetworkIDParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        PVisitedNetworkIDList visitedNetworkIDList = new PVisitedNetworkIDList();
        if (debug) {
            dbg_enter("VisitedNetworkIDParser.parse");
        }
        try {
            char la;
            this.lexer.match(TokenTypes.P_VISITED_NETWORK_ID);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            while (true) {
                PVisitedNetworkID visitedNetworkID = new PVisitedNetworkID();
                if (this.lexer.lookAhead(0) == '\"') {
                    parseQuotedString(visitedNetworkID);
                } else {
                    parseToken(visitedNetworkID);
                }
                visitedNetworkIDList.add((SIPHeader) visitedNetworkID);
                this.lexer.SPorHT();
                la = this.lexer.lookAhead(0);
                if (la != ',') {
                    break;
                }
                this.lexer.match(44);
                this.lexer.SPorHT();
            }
            if (la == 10) {
                return visitedNetworkIDList;
            }
            throw createParseException("unexpected char = " + la);
        } finally {
            if (debug) {
                dbg_leave("VisitedNetworkIDParser.parse");
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Missing block: B:16:0x0039, code skipped:
            r6.setVisitedNetworkID(r1.toString());
            super.parse(r6);
     */
    public void parseQuotedString(org.jitsi.gov.nist.javax.sip.header.ims.PVisitedNetworkID r6) throws java.text.ParseException {
        /*
        r5 = this;
        r4 = 34;
        r2 = debug;
        if (r2 == 0) goto L_0x000b;
    L_0x0006:
        r2 = "parseQuotedString";
        r5.dbg_enter(r2);
    L_0x000b:
        r1 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0020 }
        r1.<init>();	 Catch:{ all -> 0x0020 }
        r2 = r5.lexer;	 Catch:{ all -> 0x0020 }
        r3 = 0;
        r2 = r2.lookAhead(r3);	 Catch:{ all -> 0x0020 }
        if (r2 == r4) goto L_0x002b;
    L_0x0019:
        r2 = "unexpected char";
        r2 = r5.createParseException(r2);	 Catch:{ all -> 0x0020 }
        throw r2;	 Catch:{ all -> 0x0020 }
    L_0x0020:
        r2 = move-exception;
        r3 = debug;
        if (r3 == 0) goto L_0x002a;
    L_0x0025:
        r3 = "parseQuotedString.parse";
        r5.dbg_leave(r3);
    L_0x002a:
        throw r2;
    L_0x002b:
        r2 = r5.lexer;	 Catch:{ all -> 0x0020 }
        r3 = 1;
        r2.consume(r3);	 Catch:{ all -> 0x0020 }
    L_0x0031:
        r2 = r5.lexer;	 Catch:{ all -> 0x0020 }
        r0 = r2.getNextChar();	 Catch:{ all -> 0x0020 }
        if (r0 != r4) goto L_0x004d;
    L_0x0039:
        r2 = r1.toString();	 Catch:{ all -> 0x0020 }
        r6.setVisitedNetworkID(r2);	 Catch:{ all -> 0x0020 }
        super.parse(r6);	 Catch:{ all -> 0x0020 }
        r2 = debug;
        if (r2 == 0) goto L_0x004c;
    L_0x0047:
        r2 = "parseQuotedString.parse";
        r5.dbg_leave(r2);
    L_0x004c:
        return;
    L_0x004d:
        if (r0 != 0) goto L_0x0058;
    L_0x004f:
        r2 = new java.text.ParseException;	 Catch:{ all -> 0x0020 }
        r3 = "unexpected EOL";
        r4 = 1;
        r2.<init>(r3, r4);	 Catch:{ all -> 0x0020 }
        throw r2;	 Catch:{ all -> 0x0020 }
    L_0x0058:
        r2 = 92;
        if (r0 != r2) goto L_0x0069;
    L_0x005c:
        r1.append(r0);	 Catch:{ all -> 0x0020 }
        r2 = r5.lexer;	 Catch:{ all -> 0x0020 }
        r0 = r2.getNextChar();	 Catch:{ all -> 0x0020 }
        r1.append(r0);	 Catch:{ all -> 0x0020 }
        goto L_0x0031;
    L_0x0069:
        r1.append(r0);	 Catch:{ all -> 0x0020 }
        goto L_0x0031;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.parser.ims.PVisitedNetworkIDParser.parseQuotedString(org.jitsi.gov.nist.javax.sip.header.ims.PVisitedNetworkID):void");
    }

    /* access modifiers changed from: protected */
    public void parseToken(PVisitedNetworkID visitedNetworkID) throws ParseException {
        this.lexer.match(4095);
        visitedNetworkID.setVisitedNetworkID(this.lexer.getNextToken());
        super.parse(visitedNetworkID);
    }
}
