package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.core.HostNameParser;
import org.jitsi.gov.nist.core.NameValue;
import org.jitsi.gov.nist.core.Token;
import org.jitsi.gov.nist.javax.sip.header.Protocol;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;
import org.jitsi.gov.nist.javax.sip.header.Via;
import org.jitsi.gov.nist.javax.sip.header.ViaList;

public class ViaParser extends HeaderParser {
    public ViaParser(String via) {
        super(via);
    }

    public ViaParser(Lexer lexer) {
        super(lexer);
    }

    private void parseVia(Via v) throws ParseException {
        this.lexer.match(4095);
        Token protocolName = this.lexer.getNextToken();
        this.lexer.SPorHT();
        this.lexer.match(47);
        this.lexer.SPorHT();
        this.lexer.match(4095);
        this.lexer.SPorHT();
        Token protocolVersion = this.lexer.getNextToken();
        this.lexer.SPorHT();
        this.lexer.match(47);
        this.lexer.SPorHT();
        this.lexer.match(4095);
        this.lexer.SPorHT();
        Token transport = this.lexer.getNextToken();
        this.lexer.SPorHT();
        Protocol protocol = new Protocol();
        protocol.setProtocolName(protocolName.getTokenValue());
        protocol.setProtocolVersion(protocolVersion.getTokenValue());
        protocol.setTransport(transport.getTokenValue());
        v.setSentProtocol(protocol);
        v.setSentBy(new HostNameParser(getLexer()).hostPort(true));
        this.lexer.SPorHT();
        while (this.lexer.lookAhead(0) == ';') {
            this.lexer.consume(1);
            this.lexer.SPorHT();
            NameValue nameValue = nameValue();
            if (nameValue.getName().equals("branch") && ((String) nameValue.getValueAsObject()) == null) {
                throw new ParseException("null branch Id", this.lexer.getPtr());
            }
            v.setParameter(nameValue);
            this.lexer.SPorHT();
        }
        if (this.lexer.lookAhead(0) == '(') {
            this.lexer.selectLexer("charLexer");
            this.lexer.consume(1);
            StringBuilder comment = new StringBuilder();
            while (true) {
                char ch = this.lexer.lookAhead(0);
                if (ch != ')') {
                    if (ch != '\\') {
                        if (ch == 10) {
                            break;
                        }
                        comment.append(ch);
                        this.lexer.consume(1);
                    } else {
                        comment.append(this.lexer.getNextToken().getTokenValue());
                        this.lexer.consume(1);
                        comment.append(this.lexer.getNextToken().getTokenValue());
                        this.lexer.consume(1);
                    }
                } else {
                    this.lexer.consume(1);
                    break;
                }
            }
            v.setComment(comment.toString());
        }
    }

    /* access modifiers changed from: protected */
    public NameValue nameValue() throws ParseException {
        if (debug) {
            dbg_enter("nameValue");
        }
        Token name;
        NameValue nameValue;
        String str;
        try {
            this.lexer.match(4095);
            name = this.lexer.getNextToken();
            this.lexer.SPorHT();
            boolean quoted = false;
            if (this.lexer.lookAhead(0) == '=') {
                String str2;
                this.lexer.consume(1);
                this.lexer.SPorHT();
                if (name.getTokenValue().compareToIgnoreCase("received") == 0) {
                    str2 = this.lexer.byteStringNoSemicolon();
                } else if (this.lexer.lookAhead(0) == '\"') {
                    str2 = this.lexer.quotedString();
                    quoted = true;
                } else {
                    this.lexer.match(4095);
                    str2 = this.lexer.getNextToken().getTokenValue();
                }
                nameValue = new NameValue(name.getTokenValue().toLowerCase(), str2);
                if (quoted) {
                    nameValue.setQuotedValue();
                }
                if (debug) {
                    str = "nameValue";
                    dbg_leave(str);
                }
                return nameValue;
            }
            nameValue = new NameValue(name.getTokenValue().toLowerCase(), null);
            if (debug) {
                str = "nameValue";
                dbg_leave(str);
            }
            return nameValue;
        } catch (ParseException e) {
            nameValue = new NameValue(name.getTokenValue(), null);
            if (debug) {
                str = "nameValue";
            }
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("nameValue");
            }
        }
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("parse");
        }
        try {
            ViaList viaList = new ViaList();
            this.lexer.match(TokenTypes.VIA);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            while (true) {
                Via v = new Via();
                parseVia(v);
                viaList.add((SIPHeader) v);
                this.lexer.SPorHT();
                if (this.lexer.lookAhead(0) == ',') {
                    this.lexer.consume(1);
                    this.lexer.SPorHT();
                }
                if (this.lexer.lookAhead(0) == 10) {
                    break;
                }
            }
            this.lexer.match(10);
            return viaList;
        } finally {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }
}
