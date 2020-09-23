package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.address.AddressImpl;
import org.jitsi.gov.nist.javax.sip.address.GenericURI;

public class AddressParser extends Parser {
    public AddressParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("charLexer");
    }

    public AddressParser(String address) {
        this.lexer = new Lexer("charLexer", address);
    }

    /* access modifiers changed from: protected */
    public AddressImpl nameAddr() throws ParseException {
        if (debug) {
            dbg_enter("nameAddr");
        }
        try {
            GenericURI uri;
            AddressImpl retval;
            if (this.lexer.lookAhead(0) == '<') {
                this.lexer.consume(1);
                this.lexer.selectLexer("sip_urlLexer");
                this.lexer.SPorHT();
                uri = new URLParser((Lexer) this.lexer).uriReference(true);
                retval = new AddressImpl();
                retval.setAddressType(1);
                retval.setURI(uri);
                this.lexer.SPorHT();
                this.lexer.match(62);
                return retval;
            }
            String name;
            AddressImpl addr = new AddressImpl();
            addr.setAddressType(1);
            if (this.lexer.lookAhead(0) == '\"') {
                name = this.lexer.quotedString();
                this.lexer.SPorHT();
            } else {
                name = this.lexer.getNextToken('<');
            }
            addr.setDisplayName(name.trim());
            this.lexer.match(60);
            this.lexer.SPorHT();
            uri = new URLParser((Lexer) this.lexer).uriReference(true);
            retval = new AddressImpl();
            addr.setAddressType(1);
            addr.setURI(uri);
            this.lexer.SPorHT();
            this.lexer.match(62);
            if (debug) {
                dbg_leave("nameAddr");
            }
            return addr;
        } finally {
            if (debug) {
                dbg_leave("nameAddr");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x004f  */
    public org.jitsi.gov.nist.javax.sip.address.AddressImpl address(boolean r12) throws java.text.ParseException {
        /*
        r11 = this;
        r10 = 60;
        r9 = 58;
        r8 = 47;
        r7 = 34;
        r6 = debug;
        if (r6 == 0) goto L_0x0011;
    L_0x000c:
        r6 = "address";
        r11.dbg_enter(r6);
    L_0x0011:
        r2 = 0;
        r0 = 0;
    L_0x0013:
        r6 = r11.lexer;	 Catch:{ all -> 0x004a }
        r6 = r6.hasMoreChars();	 Catch:{ all -> 0x004a }
        if (r6 == 0) goto L_0x0029;
    L_0x001b:
        r6 = r11.lexer;	 Catch:{ all -> 0x004a }
        r1 = r6.lookAhead(r0);	 Catch:{ all -> 0x004a }
        if (r1 == r10) goto L_0x0029;
    L_0x0023:
        if (r1 == r7) goto L_0x0029;
    L_0x0025:
        if (r1 == r9) goto L_0x0029;
    L_0x0027:
        if (r1 != r8) goto L_0x0041;
    L_0x0029:
        r6 = r11.lexer;	 Catch:{ all -> 0x004a }
        r1 = r6.lookAhead(r0);	 Catch:{ all -> 0x004a }
        if (r1 == r10) goto L_0x0033;
    L_0x0031:
        if (r1 != r7) goto L_0x0058;
    L_0x0033:
        r2 = r11.nameAddr();	 Catch:{ all -> 0x004a }
    L_0x0037:
        r6 = debug;
        if (r6 == 0) goto L_0x0040;
    L_0x003b:
        r6 = "address";
        r11.dbg_leave(r6);
    L_0x0040:
        return r2;
    L_0x0041:
        if (r1 != 0) goto L_0x0055;
    L_0x0043:
        r6 = "unexpected EOL";
        r6 = r11.createParseException(r6);	 Catch:{ all -> 0x004a }
        throw r6;	 Catch:{ all -> 0x004a }
    L_0x004a:
        r6 = move-exception;
    L_0x004b:
        r7 = debug;
        if (r7 == 0) goto L_0x0054;
    L_0x004f:
        r7 = "address";
        r11.dbg_leave(r7);
    L_0x0054:
        throw r6;
    L_0x0055:
        r0 = r0 + 1;
        goto L_0x0013;
    L_0x0058:
        if (r1 == r9) goto L_0x005c;
    L_0x005a:
        if (r1 != r8) goto L_0x0077;
    L_0x005c:
        r3 = new org.jitsi.gov.nist.javax.sip.address.AddressImpl;	 Catch:{ all -> 0x004a }
        r3.m1084init();	 Catch:{ all -> 0x004a }
        r5 = new org.jitsi.gov.nist.javax.sip.parser.URLParser;	 Catch:{ all -> 0x007e }
        r6 = r11.lexer;	 Catch:{ all -> 0x007e }
        r6 = (org.jitsi.gov.nist.javax.sip.parser.Lexer) r6;	 Catch:{ all -> 0x007e }
        r5.m1452init(r6);	 Catch:{ all -> 0x007e }
        r4 = r5.uriReference(r12);	 Catch:{ all -> 0x007e }
        r6 = 2;
        r3.setAddressType(r6);	 Catch:{ all -> 0x007e }
        r3.setURI(r4);	 Catch:{ all -> 0x007e }
        r2 = r3;
        goto L_0x0037;
    L_0x0077:
        r6 = "Bad address spec";
        r6 = r11.createParseException(r6);	 Catch:{ all -> 0x004a }
        throw r6;	 Catch:{ all -> 0x004a }
    L_0x007e:
        r6 = move-exception;
        r2 = r3;
        goto L_0x004b;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.javax.sip.parser.AddressParser.address(boolean):org.jitsi.gov.nist.javax.sip.address.AddressImpl");
    }
}
