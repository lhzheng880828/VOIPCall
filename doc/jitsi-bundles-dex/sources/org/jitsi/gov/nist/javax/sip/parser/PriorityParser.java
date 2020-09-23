package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.javax.sip.header.Priority;
import org.jitsi.gov.nist.javax.sip.header.SIPHeader;

public class PriorityParser extends HeaderParser {
    public PriorityParser(String priority) {
        super(priority);
    }

    protected PriorityParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("PriorityParser.parse");
        }
        Priority priority = new Priority();
        try {
            headerName(TokenTypes.PRIORITY);
            priority.setHeaderName("Priority");
            this.lexer.SPorHT();
            priority.setPriority(this.lexer.ttokenSafe());
            this.lexer.SPorHT();
            this.lexer.match(10);
            return priority;
        } finally {
            if (debug) {
                dbg_leave("PriorityParser.parse");
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] p = new String[]{"Priority: 8;a\n"};
        for (String priorityParser : p) {
            System.out.println("encoded = " + ((Priority) new PriorityParser(priorityParser).parse()).encode());
        }
    }
}
