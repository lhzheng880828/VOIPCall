package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.core.HostNameParser;
import org.jitsi.gov.nist.core.LexerCore;
import org.jitsi.gov.nist.core.NameValue;
import org.jitsi.gov.nist.core.NameValueList;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.core.StringTokenizer;
import org.jitsi.gov.nist.core.Token;
import org.jitsi.gov.nist.javax.sip.address.GenericURI;
import org.jitsi.gov.nist.javax.sip.address.SipUri;
import org.jitsi.gov.nist.javax.sip.address.TelURLImpl;
import org.jitsi.gov.nist.javax.sip.address.TelephoneNumber;

public class URLParser extends Parser {
    public URLParser(String url) {
        this.lexer = new Lexer("sip_urlLexer", url);
    }

    public URLParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("sip_urlLexer");
    }

    protected static boolean isMark(char next) {
        switch (next) {
            case '!':
            case '\'':
            case '(':
            case ')':
            case '*':
            case '-':
            case '.':
            case '_':
            case '~':
                return true;
            default:
                return false;
        }
    }

    protected static boolean isUnreserved(char next) {
        return StringTokenizer.isAlphaDigit(next) || isMark(next);
    }

    protected static boolean isReservedNoSlash(char next) {
        switch (next) {
            case '$':
            case '&':
            case '+':
            case ',':
            case ':':
            case ';':
            case '?':
            case '@':
                return true;
            default:
                return false;
        }
    }

    protected static boolean isUserUnreserved(char la) {
        switch (la) {
            case '#':
            case '$':
            case '&':
            case '+':
            case ',':
            case '/':
            case ';':
            case '=':
            case '?':
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: protected */
    public String unreserved() throws ParseException {
        char next = this.lexer.lookAhead(0);
        if (isUnreserved(next)) {
            this.lexer.consume(1);
            return String.valueOf(next);
        }
        throw createParseException("unreserved");
    }

    /* access modifiers changed from: protected */
    public String paramNameOrValue() throws ParseException {
        int startIdx = this.lexer.getPtr();
        while (this.lexer.hasMoreChars()) {
            char next = this.lexer.lookAhead(0);
            boolean isValidChar = false;
            switch (next) {
                case '$':
                case '&':
                case '+':
                case '/':
                case ':':
                case '[':
                case ']':
                    isValidChar = true;
                    break;
            }
            if (isValidChar || isUnreserved(next)) {
                this.lexer.consume(1);
            } else if (!isEscaped()) {
                return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
            } else {
                this.lexer.consume(3);
            }
        }
        return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
    }

    private NameValue uriParam() throws ParseException {
        if (debug) {
            dbg_enter("uriParam");
        }
        String pvalue = "";
        try {
            NameValue nameValue;
            String str;
            String pname = paramNameOrValue();
            boolean isFlagParam = true;
            if (this.lexer.lookAhead(0) == '=') {
                this.lexer.consume(1);
                pvalue = paramNameOrValue();
                isFlagParam = false;
            }
            if (pname.length() == 0 && (pvalue == null || pvalue.length() == 0)) {
                nameValue = null;
                if (debug) {
                    str = "uriParam";
                }
                return nameValue;
            }
            nameValue = new NameValue(pname, pvalue, isFlagParam);
            if (debug) {
                str = "uriParam";
            }
            return nameValue;
            dbg_leave(str);
            return nameValue;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("uriParam");
            }
        }
    }

    protected static boolean isReserved(char next) {
        switch (next) {
            case '$':
            case '&':
            case '+':
            case ',':
            case '/':
            case ':':
            case ';':
            case '=':
            case '?':
            case '@':
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: protected */
    public String reserved() throws ParseException {
        char next = this.lexer.lookAhead(0);
        if (isReserved(next)) {
            this.lexer.consume(1);
            return next;
        }
        throw createParseException("reserved");
    }

    /* access modifiers changed from: protected */
    public boolean isEscaped() {
        try {
            if (this.lexer.lookAhead(0) == '%' && StringTokenizer.isHexDigit(this.lexer.lookAhead(1)) && StringTokenizer.isHexDigit(this.lexer.lookAhead(2))) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    public String escaped() throws ParseException {
        if (debug) {
            dbg_enter("escaped");
        }
        try {
            StringBuilder retval = new StringBuilder();
            char next = this.lexer.lookAhead(0);
            char next1 = this.lexer.lookAhead(1);
            char next2 = this.lexer.lookAhead(2);
            if (next == '%' && StringTokenizer.isHexDigit(next1) && StringTokenizer.isHexDigit(next2)) {
                this.lexer.consume(3);
                retval.append(next);
                retval.append(next1);
                retval.append(next2);
                String stringBuilder = retval.toString();
                return stringBuilder;
            }
            throw createParseException("escaped");
        } finally {
            if (debug) {
                dbg_leave("escaped");
            }
        }
    }

    /* access modifiers changed from: protected */
    public String mark() throws ParseException {
        if (debug) {
            dbg_enter("mark");
        }
        try {
            if (isMark(this.lexer.lookAhead(0))) {
                this.lexer.consume(1);
                String str = new String(new char[]{next});
                return str;
            }
            throw createParseException("mark");
        } finally {
            if (debug) {
                dbg_leave("mark");
            }
        }
    }

    /* access modifiers changed from: protected */
    public String uric() {
        if (debug) {
            dbg_enter("uric");
        }
        try {
            String retval;
            String str;
            char la = this.lexer.lookAhead(0);
            if (isUnreserved(la)) {
                this.lexer.consume(1);
                retval = LexerCore.charAsString(la);
                if (!debug) {
                    return retval;
                }
                str = "uric";
            } else if (isReserved(la)) {
                this.lexer.consume(1);
                retval = LexerCore.charAsString(la);
                if (!debug) {
                    return retval;
                }
                str = "uric";
            } else if (isEscaped()) {
                retval = this.lexer.charAsString(3);
                this.lexer.consume(3);
                if (!debug) {
                    return retval;
                }
                str = "uric";
            } else {
                if (debug) {
                    dbg_leave("uric");
                }
                return null;
            }
            dbg_leave(str);
            return retval;
        } catch (Exception e) {
            if (debug) {
                dbg_leave("uric");
            }
            return null;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("uric");
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public String uricNoSlash() {
        if (debug) {
            dbg_enter("uricNoSlash");
        }
        try {
            String charAsString;
            String str;
            char la = this.lexer.lookAhead(0);
            if (isEscaped()) {
                charAsString = this.lexer.charAsString(3);
                this.lexer.consume(3);
                if (!debug) {
                    return charAsString;
                }
                str = "uricNoSlash";
            } else if (isUnreserved(la)) {
                this.lexer.consume(1);
                charAsString = LexerCore.charAsString(la);
                if (!debug) {
                    return charAsString;
                }
                str = "uricNoSlash";
            } else if (isReservedNoSlash(la)) {
                this.lexer.consume(1);
                charAsString = LexerCore.charAsString(la);
                if (!debug) {
                    return charAsString;
                }
                str = "uricNoSlash";
            } else {
                if (debug) {
                    dbg_leave("uricNoSlash");
                }
                return null;
            }
            dbg_leave(str);
            return charAsString;
        } catch (ParseException e) {
            if (debug) {
                dbg_leave("uricNoSlash");
            }
            return null;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("uricNoSlash");
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public String uricString() throws ParseException {
        StringBuilder retval = new StringBuilder();
        while (true) {
            String next = uric();
            if (next != null) {
                retval.append(next);
            } else if (this.lexer.lookAhead(0) != '[') {
                return retval.toString();
            } else {
                retval.append(new HostNameParser(getLexer()).hostPort(false).toString());
            }
        }
    }

    public GenericURI uriReference(boolean inBrackets) throws ParseException {
        if (debug) {
            dbg_enter("uriReference");
        }
        Token[] tokens = this.lexer.peekNextToken(2);
        Token t1 = tokens[0];
        Token t2 = tokens[1];
        try {
            GenericURI retval;
            if (t1.getTokenType() == TokenTypes.SIP || t1.getTokenType() == TokenTypes.SIPS) {
                if (t2.getTokenType() == 58) {
                    retval = sipURL(inBrackets);
                } else {
                    throw createParseException("Expecting ':'");
                }
            } else if (t1.getTokenType() != TokenTypes.TEL) {
                retval = new GenericURI(uricString());
            } else if (t2.getTokenType() == 58) {
                retval = telURL(inBrackets);
            } else {
                throw createParseException("Expecting ':'");
            }
            if (debug) {
                dbg_leave("uriReference");
            }
            return retval;
        } catch (ParseException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("uriReference");
            }
        }
    }

    private String base_phone_number() throws ParseException {
        String stringBuilder;
        StringBuilder s = new StringBuilder();
        if (debug) {
            dbg_enter("base_phone_number");
        }
        int lc = 0;
        while (this.lexer.hasMoreChars()) {
            try {
                char w = this.lexer.lookAhead(0);
                if (StringTokenizer.isDigit(w) || w == '-' || w == '.' || w == '(' || w == ')') {
                    this.lexer.consume(1);
                    s.append(w);
                    lc++;
                } else {
                    if (lc <= 0) {
                        throw createParseException("unexpected " + w);
                    }
                    stringBuilder = s.toString();
                    return stringBuilder;
                }
            } finally {
                if (debug) {
                    dbg_leave("base_phone_number");
                }
            }
        }
        stringBuilder = s.toString();
        return stringBuilder;
    }

    private String local_number() throws ParseException {
        String stringBuilder;
        StringBuilder s = new StringBuilder();
        if (debug) {
            dbg_enter("local_number");
        }
        int lc = 0;
        while (this.lexer.hasMoreChars()) {
            try {
                char la = this.lexer.lookAhead(0);
                if (la == '*' || la == '#' || la == '-' || la == '.' || la == '(' || la == ')' || StringTokenizer.isHexDigit(la)) {
                    this.lexer.consume(1);
                    s.append(la);
                    lc++;
                } else {
                    if (lc <= 0) {
                        throw createParseException("unexepcted " + la);
                    }
                    stringBuilder = s.toString();
                    return stringBuilder;
                }
            } finally {
                if (debug) {
                    dbg_leave("local_number");
                }
            }
        }
        stringBuilder = s.toString();
        return stringBuilder;
    }

    public final TelephoneNumber parseTelephoneNumber(boolean inBrackets) throws ParseException {
        if (debug) {
            dbg_enter("telephone_subscriber");
        }
        this.lexer.selectLexer("charLexer");
        try {
            TelephoneNumber tn;
            char c = this.lexer.lookAhead(0);
            if (c == '+') {
                tn = global_phone_number(inBrackets);
            } else if (StringTokenizer.isHexDigit(c) || c == '#' || c == '*' || c == '-' || c == '.' || c == '(' || c == ')') {
                tn = local_phone_number(inBrackets);
            } else {
                throw createParseException("unexpected char " + c);
            }
            if (debug) {
                dbg_leave("telephone_subscriber");
            }
            return tn;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("telephone_subscriber");
            }
        }
    }

    private final TelephoneNumber global_phone_number(boolean inBrackets) throws ParseException {
        if (debug) {
            dbg_enter("global_phone_number");
        }
        try {
            TelephoneNumber tn = new TelephoneNumber();
            tn.setGlobal(true);
            this.lexer.match(43);
            tn.setPhoneNumber(base_phone_number());
            if (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) == ';' && inBrackets) {
                this.lexer.consume(1);
                tn.setParameters(tel_parameters());
            }
            if (debug) {
                dbg_leave("global_phone_number");
            }
            return tn;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("global_phone_number");
            }
        }
    }

    private TelephoneNumber local_phone_number(boolean inBrackets) throws ParseException {
        if (debug) {
            dbg_enter("local_phone_number");
        }
        TelephoneNumber tn = new TelephoneNumber();
        tn.setGlobal(false);
        try {
            tn.setPhoneNumber(local_number());
            if (this.lexer.hasMoreChars()) {
                switch (this.lexer.peekNextToken().getTokenType()) {
                    case 59:
                        if (inBrackets) {
                            this.lexer.consume(1);
                            tn.setParameters(tel_parameters());
                            break;
                        }
                        break;
                }
            }
            if (debug) {
                dbg_leave("local_phone_number");
            }
            return tn;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("local_phone_number");
            }
        }
    }

    private NameValueList tel_parameters() throws ParseException {
        NameValueList nvList = new NameValueList();
        while (true) {
            NameValue nv;
            String pname = paramNameOrValue();
            if (pname.equalsIgnoreCase("phone-context")) {
                nv = phone_context();
            } else if (this.lexer.lookAhead(0) == '=') {
                this.lexer.consume(1);
                nv = new NameValue(pname, paramNameOrValue(), false);
            } else {
                nv = new NameValue(pname, "", true);
            }
            nvList.set(nv);
            if (this.lexer.lookAhead(0) != ';') {
                return nvList;
            }
            this.lexer.consume(1);
        }
    }

    private NameValue phone_context() throws ParseException {
        String value;
        this.lexer.match(61);
        char la = this.lexer.lookAhead(0);
        if (la == '+') {
            this.lexer.consume(1);
            value = "+" + base_phone_number();
        } else if (StringTokenizer.isAlphaDigit(la)) {
            value = this.lexer.match(4095).getTokenValue();
        } else {
            throw new ParseException("Invalid phone-context:" + la, -1);
        }
        return new NameValue("phone-context", value, false);
    }

    public TelURLImpl telURL(boolean inBrackets) throws ParseException {
        this.lexer.match(TokenTypes.TEL);
        this.lexer.match(58);
        TelephoneNumber tn = parseTelephoneNumber(inBrackets);
        TelURLImpl telUrl = new TelURLImpl();
        telUrl.setTelephoneNumber(tn);
        return telUrl;
    }

    public SipUri sipURL(boolean inBrackets) throws ParseException {
        if (debug) {
            dbg_enter("sipURL");
        }
        SipUri retval = new SipUri();
        Token nextToken = this.lexer.peekNextToken();
        int sipOrSips = TokenTypes.SIP;
        String scheme = "sip";
        if (nextToken.getTokenType() == TokenTypes.SIPS) {
            sipOrSips = TokenTypes.SIPS;
            scheme = "sips";
        }
        try {
            this.lexer.match(sipOrSips);
            this.lexer.match(58);
            retval.setScheme(scheme);
            int startOfUser = this.lexer.markInputPosition();
            String userOrHost = user();
            String passOrPort = null;
            if (this.lexer.lookAhead() == ':') {
                this.lexer.consume(1);
                passOrPort = password();
            }
            if (this.lexer.lookAhead() == '@') {
                this.lexer.consume(1);
                retval.setUser(userOrHost);
                if (passOrPort != null) {
                    retval.setUserPassword(passOrPort);
                }
            } else {
                this.lexer.rewindInputPosition(startOfUser);
            }
            retval.setHostPort(new HostNameParser(getLexer()).hostPort(false));
            this.lexer.selectLexer("charLexer");
            while (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) == ';' && inBrackets) {
                this.lexer.consume(1);
                NameValue parms = uriParam();
                if (parms != null) {
                    retval.setUriParameter(parms);
                }
            }
            if (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) == '?') {
                this.lexer.consume(1);
                while (this.lexer.hasMoreChars()) {
                    retval.setQHeader(qheader());
                    if (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) != '&') {
                        break;
                    }
                    this.lexer.consume(1);
                }
            }
            if (debug) {
                dbg_leave("sipURL");
            }
            return retval;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("sipURL");
            }
        }
    }

    public String peekScheme() throws ParseException {
        Token[] tokens = this.lexer.peekNextToken(1);
        if (tokens.length == 0) {
            return null;
        }
        return tokens[0].getTokenValue();
    }

    /* access modifiers changed from: protected */
    public NameValue qheader() throws ParseException {
        String name = this.lexer.getNextToken('=');
        this.lexer.consume(1);
        return new NameValue(name, hvalue(), false);
    }

    /* access modifiers changed from: protected */
    public String hvalue() throws ParseException {
        StringBuilder retval = new StringBuilder();
        while (this.lexer.hasMoreChars()) {
            char la = this.lexer.lookAhead(0);
            boolean isValidChar = false;
            switch (la) {
                case '!':
                case '\"':
                case '$':
                case '(':
                case ')':
                case '*':
                case '+':
                case '-':
                case '.':
                case '/':
                case ':':
                case '?':
                case '[':
                case ']':
                case '_':
                case '~':
                    isValidChar = true;
                    break;
            }
            if (isValidChar || StringTokenizer.isAlphaDigit(la)) {
                this.lexer.consume(1);
                retval.append(la);
            } else if (la != '%') {
                return retval.toString();
            } else {
                retval.append(escaped());
            }
        }
        return retval.toString();
    }

    /* access modifiers changed from: protected */
    public String urlString() throws ParseException {
        StringBuilder retval = new StringBuilder();
        this.lexer.selectLexer("charLexer");
        while (this.lexer.hasMoreChars()) {
            char la = this.lexer.lookAhead(0);
            if (la == ' ' || la == 9 || la == 10 || la == '>' || la == '<') {
                break;
            }
            this.lexer.consume(0);
            retval.append(la);
        }
        return retval.toString();
    }

    /* access modifiers changed from: protected */
    public String user() throws ParseException {
        if (debug) {
            dbg_enter("user");
        }
        try {
            int startIdx = this.lexer.getPtr();
            loop0:
            while (this.lexer.hasMoreChars()) {
                char la = this.lexer.lookAhead(0);
                if (!isUnreserved(la) && !isUserUnreserved(la)) {
                    if (!isEscaped()) {
                        break loop0;
                    }
                    this.lexer.consume(3);
                } else {
                    this.lexer.consume(1);
                }
            }
            String substring = this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
            return substring;
        } finally {
            if (debug) {
                dbg_leave("user");
            }
        }
    }

    /* access modifiers changed from: protected */
    public String password() throws ParseException {
        int startIdx = this.lexer.getPtr();
        while (true) {
            char la = this.lexer.lookAhead(0);
            boolean isValidChar = false;
            switch (la) {
                case '$':
                case '&':
                case '+':
                case ',':
                case '=':
                    isValidChar = true;
                    break;
            }
            if (isValidChar || isUnreserved(la)) {
                this.lexer.consume(1);
            } else if (!isEscaped()) {
                return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
            } else {
                this.lexer.consume(3);
            }
        }
    }

    public GenericURI parse() throws ParseException {
        return uriReference(true);
    }

    public static void main(String[] args) throws ParseException {
        String[] test = new String[]{"sip:alice@example.com", "sips:alice@examples.com", "sip:3Zqkv5dajqaaas0tCjCxT0xH2ZEuEMsFl0xoasip%3A%2B3519116786244%40siplab.domain.com@213.0.115.163:7070"};
        for (int i = 0; i < test.length; i++) {
            GenericURI uri = new URLParser(test[i]).parse();
            System.out.println("uri type returned " + uri.getClass().getName());
            System.out.println(test[i] + " is SipUri? " + uri.isSipURI() + Separators.GREATER_THAN + uri.encode());
        }
    }
}
