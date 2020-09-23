package org.jitsi.gov.nist.core;

import java.text.ParseException;

public class HostNameParser extends ParserCore {
    private static final char[] VALID_DOMAIN_LABEL_CHAR = new char[]{65533, '-', '.'};
    private static boolean stripAddressScopeZones;

    static {
        stripAddressScopeZones = false;
        stripAddressScopeZones = Boolean.getBoolean("org.jitsi.gov.nist.core.STRIP_ADDR_SCOPES");
    }

    public HostNameParser(String hname) {
        this.lexer = new LexerCore("charLexer", hname);
    }

    public HostNameParser(LexerCore lexer) {
        this.lexer = lexer;
        lexer.selectLexer("charLexer");
    }

    /* access modifiers changed from: protected */
    public void consumeDomainLabel() throws ParseException {
        if (debug) {
            dbg_enter("domainLabel");
        }
        try {
            this.lexer.consumeValidChars(VALID_DOMAIN_LABEL_CHAR);
        } finally {
            if (debug) {
                dbg_leave("domainLabel");
            }
        }
    }

    /* access modifiers changed from: protected */
    public String ipv6Reference() throws ParseException {
        StringBuilder retval = new StringBuilder();
        if (debug) {
            dbg_enter("ipv6Reference");
        }
        try {
            String stringBuilder;
            String str;
            char la;
            if (stripAddressScopeZones) {
                while (this.lexer.hasMoreChars()) {
                    la = this.lexer.lookAhead(0);
                    if (StringTokenizer.isHexDigit(la) || la == '.' || la == ':' || la == '[') {
                        this.lexer.consume(1);
                        retval.append(la);
                    } else if (la == ']') {
                        this.lexer.consume(1);
                        retval.append(la);
                        stringBuilder = retval.toString();
                        if (debug) {
                            str = "ipv6Reference";
                        }
                        return stringBuilder;
                    } else {
                        if (la == '%') {
                            this.lexer.consume(1);
                            String rest = this.lexer.getRest();
                            if (!(rest == null || rest.length() == 0)) {
                                int stripLen = rest.indexOf(93);
                                if (stripLen != -1) {
                                    this.lexer.consume(stripLen + 1);
                                    retval.append("]");
                                    stringBuilder = retval.toString();
                                    if (debug) {
                                        str = "ipv6Reference";
                                    }
                                    return stringBuilder;
                                }
                            }
                        }
                        throw new ParseException(this.lexer.getBuffer() + ": Illegal Host name ", this.lexer.getPtr());
                    }
                }
                throw new ParseException(this.lexer.getBuffer() + ": Illegal Host name ", this.lexer.getPtr());
            }
            while (this.lexer.hasMoreChars()) {
                la = this.lexer.lookAhead(0);
                if (StringTokenizer.isHexDigit(la) || la == '.' || la == ':' || la == '[') {
                    this.lexer.consume(1);
                    retval.append(la);
                } else {
                    if (la == ']') {
                        this.lexer.consume(1);
                        retval.append(la);
                        stringBuilder = retval.toString();
                        if (debug) {
                            str = "ipv6Reference";
                        }
                        return stringBuilder;
                    }
                    throw new ParseException(this.lexer.getBuffer() + ": Illegal Host name ", this.lexer.getPtr());
                }
            }
            throw new ParseException(this.lexer.getBuffer() + ": Illegal Host name ", this.lexer.getPtr());
            dbg_leave(str);
            return stringBuilder;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("ipv6Reference");
            }
        }
    }

    public Host host() throws ParseException {
        if (debug) {
            dbg_enter("host");
        }
        try {
            String hostname;
            int startPtr;
            if (this.lexer.lookAhead(0) == '[') {
                hostname = ipv6Reference();
            } else if (isIPv6Address(this.lexer.getRest())) {
                startPtr = this.lexer.getPtr();
                this.lexer.consumeValidChars(new char[]{65533, ':'});
                hostname = "[" + this.lexer.getBuffer().substring(startPtr, this.lexer.getPtr()) + "]";
            } else {
                startPtr = this.lexer.getPtr();
                consumeDomainLabel();
                hostname = this.lexer.getBuffer().substring(startPtr, this.lexer.getPtr());
            }
            if (hostname.length() == 0) {
                throw new ParseException(this.lexer.getBuffer() + ": Missing host name", this.lexer.getPtr());
            }
            Host host = new Host(hostname);
            return host;
        } finally {
            if (debug) {
                dbg_leave("host");
            }
        }
    }

    private boolean isIPv6Address(String uriHeader) {
        String hostName = uriHeader;
        int indexOfComma = uriHeader.indexOf(Separators.COMMA);
        if (indexOfComma != -1) {
            hostName = uriHeader.substring(0, indexOfComma);
        }
        int hostEnd = hostName.indexOf(63);
        int semiColonIndex = hostName.indexOf(59);
        if (hostEnd == -1 || (semiColonIndex != -1 && hostEnd > semiColonIndex)) {
            hostEnd = semiColonIndex;
        }
        if (hostEnd == -1) {
            hostEnd = hostName.length();
        }
        String host = hostName.substring(0, hostEnd);
        int firstColonIndex = host.indexOf(58);
        if (firstColonIndex == -1 || host.indexOf(58, firstColonIndex + 1) == -1) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:34:0x00b7, code skipped:
            if (stripAddressScopeZones != false) goto L_0x0085;
     */
    public org.jitsi.gov.nist.core.HostPort hostPort(boolean r10) throws java.text.ParseException {
        /*
        r9 = this;
        r5 = debug;
        if (r5 == 0) goto L_0x0009;
    L_0x0004:
        r5 = "hostPort";
        r9.dbg_enter(r5);
    L_0x0009:
        r0 = r9.host();	 Catch:{ all -> 0x0060 }
        r1 = new org.jitsi.gov.nist.core.HostPort;	 Catch:{ all -> 0x0060 }
        r1.m1034init();	 Catch:{ all -> 0x0060 }
        r1.setHost(r0);	 Catch:{ all -> 0x0060 }
        if (r10 == 0) goto L_0x001c;
    L_0x0017:
        r5 = r9.lexer;	 Catch:{ all -> 0x0060 }
        r5.SPorHT();	 Catch:{ all -> 0x0060 }
    L_0x001c:
        r5 = r9.lexer;	 Catch:{ all -> 0x0060 }
        r5 = r5.hasMoreChars();	 Catch:{ all -> 0x0060 }
        if (r5 == 0) goto L_0x0085;
    L_0x0024:
        r5 = r9.lexer;	 Catch:{ all -> 0x0060 }
        r6 = 0;
        r2 = r5.lookAhead(r6);	 Catch:{ all -> 0x0060 }
        switch(r2) {
            case 9: goto L_0x0085;
            case 10: goto L_0x0085;
            case 13: goto L_0x0085;
            case 32: goto L_0x0085;
            case 37: goto L_0x00b5;
            case 44: goto L_0x0085;
            case 47: goto L_0x0085;
            case 58: goto L_0x006b;
            case 59: goto L_0x0085;
            case 62: goto L_0x0085;
            case 63: goto L_0x0085;
            default: goto L_0x002e;
        };	 Catch:{ all -> 0x0060 }
    L_0x002e:
        if (r10 != 0) goto L_0x0085;
    L_0x0030:
        r5 = new java.text.ParseException;	 Catch:{ all -> 0x0060 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0060 }
        r6.<init>();	 Catch:{ all -> 0x0060 }
        r7 = r9.lexer;	 Catch:{ all -> 0x0060 }
        r7 = r7.getBuffer();	 Catch:{ all -> 0x0060 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0060 }
        r7 = " Illegal character in hostname:";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0060 }
        r7 = r9.lexer;	 Catch:{ all -> 0x0060 }
        r8 = 0;
        r7 = r7.lookAhead(r8);	 Catch:{ all -> 0x0060 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0060 }
        r6 = r6.toString();	 Catch:{ all -> 0x0060 }
        r7 = r9.lexer;	 Catch:{ all -> 0x0060 }
        r7 = r7.getPtr();	 Catch:{ all -> 0x0060 }
        r5.<init>(r6, r7);	 Catch:{ all -> 0x0060 }
        throw r5;	 Catch:{ all -> 0x0060 }
    L_0x0060:
        r5 = move-exception;
        r6 = debug;
        if (r6 == 0) goto L_0x006a;
    L_0x0065:
        r6 = "hostPort";
        r9.dbg_leave(r6);
    L_0x006a:
        throw r5;
    L_0x006b:
        r5 = r9.lexer;	 Catch:{ all -> 0x0060 }
        r6 = 1;
        r5.consume(r6);	 Catch:{ all -> 0x0060 }
        if (r10 == 0) goto L_0x0078;
    L_0x0073:
        r5 = r9.lexer;	 Catch:{ all -> 0x0060 }
        r5.SPorHT();	 Catch:{ all -> 0x0060 }
    L_0x0078:
        r5 = r9.lexer;	 Catch:{ NumberFormatException -> 0x008f }
        r4 = r5.number();	 Catch:{ NumberFormatException -> 0x008f }
        r5 = java.lang.Integer.parseInt(r4);	 Catch:{ NumberFormatException -> 0x008f }
        r1.setPort(r5);	 Catch:{ NumberFormatException -> 0x008f }
    L_0x0085:
        r5 = debug;
        if (r5 == 0) goto L_0x008e;
    L_0x0089:
        r5 = "hostPort";
        r9.dbg_leave(r5);
    L_0x008e:
        return r1;
    L_0x008f:
        r3 = move-exception;
        r5 = new java.text.ParseException;	 Catch:{ all -> 0x0060 }
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0060 }
        r6.<init>();	 Catch:{ all -> 0x0060 }
        r7 = r9.lexer;	 Catch:{ all -> 0x0060 }
        r7 = r7.getBuffer();	 Catch:{ all -> 0x0060 }
        r6 = r6.append(r7);	 Catch:{ all -> 0x0060 }
        r7 = " :Error parsing port ";
        r6 = r6.append(r7);	 Catch:{ all -> 0x0060 }
        r6 = r6.toString();	 Catch:{ all -> 0x0060 }
        r7 = r9.lexer;	 Catch:{ all -> 0x0060 }
        r7 = r7.getPtr();	 Catch:{ all -> 0x0060 }
        r5.<init>(r6, r7);	 Catch:{ all -> 0x0060 }
        throw r5;	 Catch:{ all -> 0x0060 }
    L_0x00b5:
        r5 = stripAddressScopeZones;	 Catch:{ all -> 0x0060 }
        if (r5 == 0) goto L_0x002e;
    L_0x00b9:
        goto L_0x0085;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.core.HostNameParser.hostPort(boolean):org.jitsi.gov.nist.core.HostPort");
    }

    public static void main(String[] args) throws ParseException {
        String[] hostNames = new String[]{"foo.bar.com:1234", "proxima.chaplin.bt.co.uk", "129.6.55.181:2345", ":1234", "foo.bar.com:         1234", "foo.bar.com     :      1234   ", "MIK_S:1234"};
        for (int i = 0; i < hostNames.length; i++) {
            try {
                System.out.println("[" + new HostNameParser(hostNames[i]).hostPort(true).encode() + "]");
            } catch (ParseException ex) {
                System.out.println("exception text = " + ex.getMessage());
            }
        }
    }
}
