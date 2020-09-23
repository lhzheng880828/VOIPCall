package org.jitsi.gov.nist.core;

import java.text.ParseException;

public abstract class ParserCore {
    public static final boolean debug = Debug.parserDebug;
    static int nesting_level;
    protected LexerCore lexer;

    /* access modifiers changed from: protected */
    public NameValue nameValue(char separator) throws ParseException {
        NameValue nv;
        if (debug) {
            dbg_enter("nameValue");
        }
        Token name;
        try {
            this.lexer.match(4095);
            name = this.lexer.getNextToken();
            this.lexer.SPorHT();
            boolean quoted = false;
            if (this.lexer.lookAhead(0) == separator) {
                String str;
                this.lexer.consume(1);
                this.lexer.SPorHT();
                boolean isFlag = false;
                if (this.lexer.lookAhead(0) == '\"') {
                    str = this.lexer.quotedString();
                    quoted = true;
                } else {
                    this.lexer.match(4095);
                    str = this.lexer.getNextToken().tokenValue;
                    if (str == null) {
                        str = "";
                        isFlag = true;
                    }
                }
                nv = new NameValue(name.tokenValue, str, isFlag);
                if (quoted) {
                    nv.setQuotedValue();
                }
                if (debug) {
                    dbg_leave("nameValue");
                }
            } else {
                nv = new NameValue(name.tokenValue, "", true);
                if (debug) {
                    dbg_leave("nameValue");
                }
            }
        } catch (ParseException e) {
            nv = new NameValue(name.tokenValue, null, false);
            if (debug) {
                dbg_leave("nameValue");
            }
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("nameValue");
            }
        }
        return nv;
    }

    /* access modifiers changed from: protected */
    public void dbg_enter(String rule) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < nesting_level; i++) {
            stringBuilder.append(Separators.GREATER_THAN);
        }
        if (debug) {
            System.out.println(stringBuilder + rule + "\nlexer buffer = \n" + this.lexer.getRest());
        }
        nesting_level++;
    }

    /* access modifiers changed from: protected */
    public void dbg_leave(String rule) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < nesting_level; i++) {
            stringBuilder.append(Separators.LESS_THAN);
        }
        if (debug) {
            System.out.println(stringBuilder + rule + "\nlexer buffer = \n" + this.lexer.getRest());
        }
        nesting_level--;
    }

    /* access modifiers changed from: protected */
    public NameValue nameValue() throws ParseException {
        return nameValue('=');
    }

    /* access modifiers changed from: protected */
    public void peekLine(String rule) {
        if (debug) {
            Debug.println(rule + Separators.SP + this.lexer.peekLine());
        }
    }
}
