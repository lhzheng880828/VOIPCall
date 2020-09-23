package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.WarningHeader;

public class Warning extends SIPHeader implements WarningHeader {
    private static final long serialVersionUID = -3433328864230783899L;
    protected String agent;
    protected int code;
    protected String text;

    public Warning() {
        super("Warning");
    }

    public StringBuilder encodeBody(StringBuilder buffer) {
        return this.text != null ? buffer.append(Integer.toString(this.code)).append(Separators.SP).append(this.agent).append(Separators.SP).append(Separators.DOUBLE_QUOTE).append(this.text).append(Separators.DOUBLE_QUOTE) : buffer.append(Integer.toString(this.code)).append(Separators.SP).append(this.agent);
    }

    public int getCode() {
        return this.code;
    }

    public String getAgent() {
        return this.agent;
    }

    public String getText() {
        return this.text;
    }

    public void setCode(int code) throws InvalidArgumentException {
        if (code <= 99 || code >= 1000) {
            throw new InvalidArgumentException("Code parameter in the Warning header is invalid: code=" + code);
        }
        this.code = code;
    }

    public void setAgent(String host) throws ParseException {
        if (host == null) {
            throw new NullPointerException("the host parameter in the Warning header is null");
        }
        this.agent = host;
    }

    public void setText(String text) throws ParseException {
        if (text == null) {
            throw new ParseException("The text parameter in the Warning header is null", 0);
        }
        this.text = text;
    }
}
