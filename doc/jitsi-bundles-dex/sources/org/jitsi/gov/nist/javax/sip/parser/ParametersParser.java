package org.jitsi.gov.nist.javax.sip.parser;

import java.text.ParseException;
import org.jitsi.gov.nist.core.NameValue;
import org.jitsi.gov.nist.javax.sip.header.ParametersHeader;

public abstract class ParametersParser extends HeaderParser {
    protected ParametersParser(Lexer lexer) {
        super(lexer);
    }

    protected ParametersParser(String buffer) {
        super(buffer);
    }

    /* access modifiers changed from: protected */
    public void parse(ParametersHeader parametersHeader) throws ParseException {
        this.lexer.SPorHT();
        while (this.lexer.lookAhead(0) == ';') {
            this.lexer.consume(1);
            this.lexer.SPorHT();
            parametersHeader.setParameter(nameValue());
            this.lexer.SPorHT();
        }
    }

    /* access modifiers changed from: protected */
    public void parseNameValueList(ParametersHeader parametersHeader) throws ParseException {
        parametersHeader.removeParameters();
        while (true) {
            this.lexer.SPorHT();
            NameValue nv = nameValue();
            parametersHeader.setParameter(nv.getName(), (String) nv.getValueAsObject());
            this.lexer.SPorHT();
            if (this.lexer.lookAhead(0) == ';') {
                this.lexer.consume(1);
            } else {
                return;
            }
        }
    }
}
