package org.jitsi.gov.nist.core;

import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jitsi.gov.nist.javax.sip.Utils;

public class LexerCore extends StringTokenizer {
    public static final int ALPHA = 4099;
    static final char ALPHADIGIT_VALID_CHARS = '�';
    static final char ALPHA_VALID_CHARS = '￿';
    public static final int AND = 38;
    public static final int AT = 64;
    public static final int BACKSLASH = 92;
    public static final int BACK_QUOTE = 96;
    public static final int BAR = 124;
    public static final int COLON = 58;
    public static final int DIGIT = 4098;
    static final char DIGIT_VALID_CHARS = '￾';
    public static final int DOLLAR = 36;
    public static final int DOT = 46;
    public static final int DOUBLEQUOTE = 34;
    public static final int END = 4096;
    public static final int EQUALS = 61;
    public static final int EXCLAMATION = 33;
    public static final int GREATER_THAN = 62;
    public static final int HAT = 94;
    public static final int HT = 9;
    public static final int ID = 4095;
    public static final int LESS_THAN = 60;
    public static final int LPAREN = 40;
    public static final int L_CURLY = 123;
    public static final int L_SQUARE_BRACKET = 91;
    public static final int MINUS = 45;
    public static final int NULL = 0;
    public static final int PERCENT = 37;
    public static final int PLUS = 43;
    public static final int POUND = 35;
    public static final int QUESTION = 63;
    public static final int QUOTE = 39;
    public static final int RPAREN = 41;
    public static final int R_CURLY = 125;
    public static final int R_SQUARE_BRACKET = 93;
    public static final int SAFE = 4094;
    public static final int SEMICOLON = 59;
    public static final int SLASH = 47;
    public static final int SP = 32;
    public static final int STAR = 42;
    public static final int START = 2048;
    public static final int TILDE = 126;
    public static final int UNDERSCORE = 95;
    public static final int WHITESPACE = 4097;
    protected static final ConcurrentHashMap<Integer, String> globalSymbolTable = new ConcurrentHashMap();
    protected static final ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> lexerTables = new ConcurrentHashMap();
    protected Map<String, Integer> currentLexer;
    protected String currentLexerName;
    protected Token currentMatch;

    /* access modifiers changed from: protected */
    public void addKeyword(String name, int value) {
        name = Utils.toUpperCase(name);
        Integer val = Integer.valueOf(value);
        this.currentLexer.put(name, val);
        globalSymbolTable.putIfAbsent(val, name);
    }

    public String lookupToken(int value) {
        if (value > 2048) {
            return (String) globalSymbolTable.get(Integer.valueOf(value));
        }
        return Character.valueOf((char) value).toString();
    }

    public void selectLexer(String lexerName) {
        this.currentLexerName = lexerName;
    }

    protected LexerCore() {
        this.currentLexer = new ConcurrentHashMap();
        this.currentLexerName = "charLexer";
    }

    public LexerCore(String lexerName, String buffer) {
        super(buffer);
        this.currentLexerName = lexerName;
    }

    public String peekNextId() {
        int oldPtr = this.ptr;
        String retval = ttoken();
        this.savedPtr = this.ptr;
        this.ptr = oldPtr;
        return retval;
    }

    public String getNextId() {
        return ttoken();
    }

    public Token getNextToken() {
        return this.currentMatch;
    }

    public Token peekNextToken() throws ParseException {
        return peekNextToken(1)[0];
    }

    public Token[] peekNextToken(int ntokens) throws ParseException {
        int old = this.ptr;
        Token[] retval = new Token[ntokens];
        for (int i = 0; i < ntokens; i++) {
            Token tok = new Token();
            if (startsId()) {
                String id = ttoken();
                tok.tokenValue = id;
                String idUppercase = Utils.toUpperCase(id);
                if (this.currentLexer.containsKey(idUppercase)) {
                    tok.tokenType = ((Integer) this.currentLexer.get(idUppercase)).intValue();
                } else {
                    tok.tokenType = 4095;
                }
            } else {
                char nextChar = getNextChar();
                tok.tokenValue = String.valueOf(nextChar);
                if (StringTokenizer.isAlpha(nextChar)) {
                    tok.tokenType = 4099;
                } else if (StringTokenizer.isDigit(nextChar)) {
                    tok.tokenType = 4098;
                } else {
                    tok.tokenType = nextChar;
                }
            }
            retval[i] = tok;
        }
        this.savedPtr = this.ptr;
        this.ptr = old;
        return retval;
    }

    public Token match(int tok) throws ParseException {
        if (Debug.parserDebug) {
            Debug.println("match " + tok);
        }
        String id;
        if (tok <= 2048 || tok >= 4096) {
            char next;
            if (tok > 4096) {
                next = lookAhead(0);
                if (tok == 4098) {
                    if (StringTokenizer.isDigit(next)) {
                        this.currentMatch = new Token();
                        this.currentMatch.tokenValue = String.valueOf(next);
                        this.currentMatch.tokenType = tok;
                        consume(1);
                    } else {
                        throw new ParseException(this.buffer + "\nExpecting DIGIT", this.ptr);
                    }
                } else if (tok == 4099) {
                    if (StringTokenizer.isAlpha(next)) {
                        this.currentMatch = new Token();
                        this.currentMatch.tokenValue = String.valueOf(next);
                        this.currentMatch.tokenType = tok;
                        consume(1);
                    } else {
                        throw new ParseException(this.buffer + "\nExpecting ALPHA", this.ptr);
                    }
                }
            }
            char ch = (char) tok;
            next = lookAhead(0);
            if (next == ch) {
                consume(1);
            } else {
                throw new ParseException(this.buffer + "\nExpecting  >>>" + ch + "<<< got >>>" + next + "<<<", this.ptr);
            }
        } else if (tok == 4095) {
            if (startsId()) {
                id = getNextId();
                this.currentMatch = new Token();
                this.currentMatch.tokenValue = id;
                this.currentMatch.tokenType = 4095;
            } else {
                throw new ParseException(this.buffer + "\nID expected", this.ptr);
            }
        } else if (tok != SAFE) {
            String nexttok = getNextId();
            Integer cur = (Integer) this.currentLexer.get(Utils.toUpperCase(nexttok));
            if (cur == null || cur.intValue() != tok) {
                throw new ParseException(this.buffer + "\nUnexpected Token : " + nexttok, this.ptr);
            }
            this.currentMatch = new Token();
            this.currentMatch.tokenValue = nexttok;
            this.currentMatch.tokenType = tok;
        } else if (startsSafeToken()) {
            id = ttokenSafe();
            this.currentMatch = new Token();
            this.currentMatch.tokenValue = id;
            this.currentMatch.tokenType = SAFE;
        } else {
            throw new ParseException(this.buffer + "\nID expected", this.ptr);
        }
        return this.currentMatch;
    }

    public void SPorHT() {
        try {
            char c = lookAhead(0);
            while (true) {
                if (c == ' ' || c == 9) {
                    consume(1);
                    c = lookAhead(0);
                } else {
                    return;
                }
            }
        } catch (ParseException e) {
        }
    }

    public static final boolean isTokenChar(char c) {
        if (StringTokenizer.isAlphaDigit(c)) {
            return true;
        }
        switch (c) {
            case '!':
            case '%':
            case '\'':
            case '*':
            case '+':
            case '-':
            case '.':
            case '_':
            case '`':
            case '~':
                return true;
            default:
                return false;
        }
    }

    public boolean startsId() {
        boolean z = false;
        try {
            return isTokenChar(lookAhead(0));
        } catch (ParseException e) {
            return z;
        }
    }

    public boolean startsSafeToken() {
        try {
            char nextChar = lookAhead(0);
            if (StringTokenizer.isAlphaDigit(nextChar)) {
                return true;
            }
            switch (nextChar) {
                case '!':
                case '\"':
                case '#':
                case '$':
                case '%':
                case '\'':
                case '*':
                case '+':
                case '-':
                case '.':
                case '/':
                case ':':
                case ';':
                case '=':
                case '?':
                case '@':
                case '[':
                case ']':
                case '^':
                case '_':
                case '`':
                case '{':
                case '|':
                case '}':
                case '~':
                    return true;
                default:
                    return false;
            }
        } catch (ParseException e) {
            return false;
        }
    }

    public String ttoken() {
        int startIdx = this.ptr;
        while (hasMoreChars() && isTokenChar(lookAhead(0))) {
            try {
                consume(1);
            } catch (ParseException e) {
                return null;
            }
        }
        return String.valueOf(this.buffer, startIdx, this.ptr - startIdx);
    }

    public String ttokenSafe() {
        int startIdx = this.ptr;
        while (hasMoreChars()) {
            try {
                char nextChar = lookAhead(0);
                if (StringTokenizer.isAlphaDigit(nextChar)) {
                    consume(1);
                } else {
                    boolean isValidChar = false;
                    switch (nextChar) {
                        case '!':
                        case '\"':
                        case '#':
                        case '$':
                        case '%':
                        case '\'':
                        case '*':
                        case '+':
                        case '-':
                        case '.':
                        case '/':
                        case ':':
                        case ';':
                        case '?':
                        case '@':
                        case '[':
                        case ']':
                        case '^':
                        case '_':
                        case '`':
                        case '{':
                        case '|':
                        case '}':
                        case '~':
                            isValidChar = true;
                            break;
                    }
                    if (!isValidChar) {
                        return String.valueOf(this.buffer, startIdx, this.ptr - startIdx);
                    }
                    consume(1);
                }
            } catch (ParseException e) {
                return null;
            }
        }
        return String.valueOf(this.buffer, startIdx, this.ptr - startIdx);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0024 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x001e A:{LOOP_END, LOOP:0: B:1:0x0003->B:12:0x001e, Catch:{ ParseException -> 0x0023 }} */
    public void consumeValidChars(char[] r9) {
        /*
        r8 = this;
        r5 = 1;
        r6 = 0;
        r4 = r9.length;
    L_0x0003:
        r7 = r8.hasMoreChars();	 Catch:{ ParseException -> 0x0023 }
        if (r7 == 0) goto L_0x0024;
    L_0x0009:
        r7 = 0;
        r2 = r8.lookAhead(r7);	 Catch:{ ParseException -> 0x0023 }
        r1 = 0;
        r0 = 0;
    L_0x0010:
        if (r0 >= r4) goto L_0x001c;
    L_0x0012:
        r3 = r9[r0];	 Catch:{ ParseException -> 0x0023 }
        switch(r3) {
            case 65533: goto L_0x002f;
            case 65534: goto L_0x002a;
            case 65535: goto L_0x0025;
            default: goto L_0x0017;
        };	 Catch:{ ParseException -> 0x0023 }
    L_0x0017:
        if (r2 != r3) goto L_0x0034;
    L_0x0019:
        r1 = r5;
    L_0x001a:
        if (r1 == 0) goto L_0x0036;
    L_0x001c:
        if (r1 == 0) goto L_0x0024;
    L_0x001e:
        r7 = 1;
        r8.consume(r7);	 Catch:{ ParseException -> 0x0023 }
        goto L_0x0003;
    L_0x0023:
        r5 = move-exception;
    L_0x0024:
        return;
    L_0x0025:
        r1 = org.jitsi.gov.nist.core.StringTokenizer.isAlpha(r2);	 Catch:{ ParseException -> 0x0023 }
        goto L_0x001a;
    L_0x002a:
        r1 = org.jitsi.gov.nist.core.StringTokenizer.isDigit(r2);	 Catch:{ ParseException -> 0x0023 }
        goto L_0x001a;
    L_0x002f:
        r1 = org.jitsi.gov.nist.core.StringTokenizer.isAlphaDigit(r2);	 Catch:{ ParseException -> 0x0023 }
        goto L_0x001a;
    L_0x0034:
        r1 = r6;
        goto L_0x001a;
    L_0x0036:
        r0 = r0 + 1;
        goto L_0x0010;
        */
        throw new UnsupportedOperationException("Method not decompiled: org.jitsi.gov.nist.core.LexerCore.consumeValidChars(char[]):void");
    }

    public String quotedString() throws ParseException {
        int startIdx = this.ptr + 1;
        if (lookAhead(0) != '\"') {
            return null;
        }
        consume(1);
        while (true) {
            char next = getNextChar();
            if (next == '\"') {
                return String.valueOf(this.buffer, startIdx, (this.ptr - startIdx) - 1);
            }
            if (next == 0) {
                throw new ParseException(String.valueOf(this.buffer) + " :unexpected EOL", this.ptr);
            } else if (next == '\\') {
                consume(1);
            }
        }
    }

    public String comment() throws ParseException {
        StringBuilder retval = new StringBuilder();
        if (lookAhead(0) != '(') {
            return null;
        }
        consume(1);
        while (true) {
            char next = getNextChar();
            if (next == ')') {
                return retval.toString();
            }
            if (next == 0) {
                throw new ParseException(this.buffer + " :unexpected EOL", this.ptr);
            } else if (next == '\\') {
                retval.append(next);
                next = getNextChar();
                if (next == 0) {
                    throw new ParseException(this.buffer + " : unexpected EOL", this.ptr);
                }
                retval.append(next);
            } else {
                retval.append(next);
            }
        }
    }

    public String byteStringNoSemicolon() {
        StringBuilder retval = new StringBuilder();
        while (true) {
            try {
                char next = lookAhead(0);
                if (next != 0 && next != 10 && next != ';' && next != ',') {
                    consume(1);
                    retval.append(next);
                }
            } catch (ParseException e) {
                return retval.toString();
            }
        }
        return retval.toString();
    }

    public String byteStringNoSlash() {
        StringBuilder retval = new StringBuilder();
        while (true) {
            try {
                char next = lookAhead(0);
                if (next != 0 && next != 10 && next != '/') {
                    consume(1);
                    retval.append(next);
                }
            } catch (ParseException e) {
                return retval.toString();
            }
        }
        return retval.toString();
    }

    public String byteStringNoComma() {
        StringBuilder retval = new StringBuilder();
        while (true) {
            try {
                char next = lookAhead(0);
                if (next == 10 || next == ',') {
                    break;
                }
                consume(1);
                retval.append(next);
            } catch (ParseException e) {
            }
        }
        return retval.toString();
    }

    public static String charAsString(char ch) {
        return String.valueOf(ch);
    }

    public String charAsString(int nchars) {
        return String.valueOf(this.buffer, this.ptr, nchars - 1);
    }

    public String number() throws ParseException {
        int startIdx = this.ptr;
        try {
            if (StringTokenizer.isDigit(lookAhead(0))) {
                consume(1);
                while (StringTokenizer.isDigit(lookAhead(0))) {
                    consume(1);
                }
                return String.valueOf(this.buffer, startIdx, this.ptr - startIdx);
            }
            throw new ParseException(this.buffer + ": Unexpected token at " + lookAhead(0), this.ptr);
        } catch (ParseException e) {
            return String.valueOf(this.buffer, startIdx, this.ptr - startIdx);
        }
    }

    public int markInputPosition() {
        return this.ptr;
    }

    public void rewindInputPosition(int position) {
        this.ptr = position;
    }

    public String getRest() {
        if (this.ptr > this.bufferLen) {
            return null;
        }
        if (this.ptr == this.bufferLen) {
            return "";
        }
        return String.valueOf(this.buffer, this.ptr, this.bufferLen - this.ptr);
    }

    public String getString(char c) throws ParseException {
        StringBuilder retval = new StringBuilder();
        while (true) {
            char next = lookAhead(0);
            if (next == 0) {
                throw new ParseException(this.buffer + "unexpected EOL", this.ptr);
            } else if (next == c) {
                consume(1);
                return retval.toString();
            } else if (next == '\\') {
                consume(1);
                char nextchar = lookAhead(0);
                if (nextchar == 0) {
                    throw new ParseException(this.buffer + "unexpected EOL", this.ptr);
                }
                consume(1);
                retval.append(nextchar);
            } else {
                consume(1);
                retval.append(next);
            }
        }
    }

    public int getPtr() {
        return this.ptr;
    }

    public String getBuffer() {
        return String.valueOf(this.buffer);
    }

    public ParseException createParseException() {
        return new ParseException(getBuffer(), this.ptr);
    }
}
