package org.jitsi.gov.nist.core;

import java.text.ParseException;
import java.util.Vector;

public class StringTokenizer {
    protected char[] buffer;
    protected int bufferLen;
    protected int ptr;
    protected int savedPtr;

    protected StringTokenizer() {
    }

    public StringTokenizer(String buffer) {
        this.buffer = buffer.toCharArray();
        this.bufferLen = buffer.length();
        this.ptr = 0;
    }

    public String nextToken() {
        int startIdx = this.ptr;
        while (this.ptr < this.bufferLen) {
            char c = this.buffer[this.ptr];
            this.ptr++;
            if (c == 10) {
                break;
            }
        }
        return String.valueOf(this.buffer, startIdx, this.ptr - startIdx);
    }

    public boolean hasMoreChars() {
        return this.ptr < this.bufferLen;
    }

    public static boolean isHexDigit(char ch) {
        return (ch >= 'A' && ch <= 'F') || ((ch >= 'a' && ch <= 'f') || isDigit(ch));
    }

    public static boolean isAlpha(char ch) {
        boolean z = false;
        if (ch > 127) {
            if (Character.isLowerCase(ch) || Character.isUpperCase(ch)) {
                z = true;
            }
            return z;
        } else if (ch >= 'a' && ch <= 'z') {
            return true;
        } else {
            if (ch < 'A' || ch > 'Z') {
                return false;
            }
            return true;
        }
    }

    public static boolean isDigit(char ch) {
        if (ch <= 127) {
            return ch <= '9' && ch >= '0';
        } else {
            return Character.isDigit(ch);
        }
    }

    public static boolean isAlphaDigit(char ch) {
        boolean z = false;
        if (ch > 127) {
            if (Character.isLowerCase(ch) || Character.isUpperCase(ch) || Character.isDigit(ch)) {
                z = true;
            }
            return z;
        } else if (ch >= 'a' && ch <= 'z') {
            return true;
        } else {
            if (ch >= 'A' && ch <= 'Z') {
                return true;
            }
            if (ch > '9' || ch < '0') {
                return false;
            }
            return true;
        }
    }

    public String getLine() {
        int startIdx = this.ptr;
        while (this.ptr < this.bufferLen && this.buffer[this.ptr] != 10) {
            this.ptr++;
        }
        if (this.ptr < this.bufferLen && this.buffer[this.ptr] == 10) {
            this.ptr++;
        }
        return String.valueOf(this.buffer, startIdx, this.ptr - startIdx);
    }

    public String peekLine() {
        int curPos = this.ptr;
        String retval = getLine();
        this.ptr = curPos;
        return retval;
    }

    public char lookAhead() throws ParseException {
        return lookAhead(0);
    }

    public char lookAhead(int k) throws ParseException {
        try {
            return this.buffer[this.ptr + k];
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
    }

    public char getNextChar() throws ParseException {
        if (this.ptr >= this.bufferLen) {
            throw new ParseException(this.buffer + " getNextChar: End of buffer", this.ptr);
        }
        char[] cArr = this.buffer;
        int i = this.ptr;
        this.ptr = i + 1;
        return cArr[i];
    }

    public void consume() {
        this.ptr = this.savedPtr;
    }

    public void consume(int k) {
        this.ptr += k;
    }

    public Vector<String> getLines() {
        Vector<String> result = new Vector();
        while (hasMoreChars()) {
            result.addElement(getLine());
        }
        return result;
    }

    public String getNextToken(char delim) throws ParseException {
        int startIdx = this.ptr;
        while (true) {
            char la = lookAhead(0);
            if (la == delim) {
                return String.valueOf(this.buffer, startIdx, this.ptr - startIdx);
            }
            if (la == 0) {
                throw new ParseException("EOL reached", 0);
            }
            consume(1);
        }
    }

    public static String getSDPFieldName(String line) {
        if (line == null) {
            return null;
        }
        try {
            return line.substring(0, line.indexOf(Separators.EQUALS));
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
