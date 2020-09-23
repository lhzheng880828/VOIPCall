package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.Utils;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.ReasonHeader;

public class Reason extends ParametersHeader implements ReasonHeader {
    private static final long serialVersionUID = -8903376965568297388L;
    public final String CAUSE = ParameterNames.CAUSE;
    public final String TEXT = "text";
    protected String protocol;

    public int getCause() {
        return getParameterAsInt(ParameterNames.CAUSE);
    }

    public void setCause(int cause) throws InvalidArgumentException {
        this.parameters.set(ParameterNames.CAUSE, Integer.valueOf(cause));
    }

    public void setProtocol(String protocol) throws ParseException {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public void setText(String text) throws ParseException {
        Object text2;
        if (text2.charAt(0) != '\"') {
            text2 = Utils.getQuotedString(text2);
        }
        this.parameters.set("text", text2);
    }

    public String getText() {
        return this.parameters.getParameter("text");
    }

    public Reason() {
        super("Reason");
    }

    public String getName() {
        return "Reason";
    }

    public StringBuilder encodeBody(StringBuilder buffer) {
        buffer.append(this.protocol);
        if (this.parameters == null || this.parameters.isEmpty()) {
            return buffer;
        }
        return this.parameters.encode(buffer.append(Separators.SEMICOLON));
    }
}
