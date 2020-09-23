package gov.nist.javax.sdp.parser;

import org.jitsi.gov.nist.core.LexerCore;
import org.jitsi.gov.nist.core.Separators;

public class Lexer extends LexerCore {
    public Lexer(String lexerName, String buffer) {
        super(lexerName, buffer);
    }

    public void selectLexer(String lexerName) {
    }

    public static String getFieldName(String line) {
        int i = line.indexOf(Separators.EQUALS);
        if (i == -1) {
            return null;
        }
        return line.substring(0, i);
    }
}
