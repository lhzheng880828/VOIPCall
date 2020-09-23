package org.jitsi.gov.nist.javax.sip.header.extensions;

import java.text.ParseException;
import java.util.Iterator;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.ParametersHeader;
import org.jitsi.javax.sip.header.ExtensionHeader;

public class References extends ParametersHeader implements ReferencesHeader, ExtensionHeader {
    private static final long serialVersionUID = 8536961681006637622L;
    private String callId;

    public References() {
        super(ReferencesHeader.NAME);
    }

    public String getCallId() {
        return this.callId;
    }

    public String getRel() {
        return getParameter(ReferencesHeader.REL);
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public void setRel(String rel) throws ParseException {
        if (rel != null) {
            setParameter(ReferencesHeader.REL, rel);
        }
    }

    public String getParameter(String name) {
        return super.getParameter(name);
    }

    public Iterator getParameterNames() {
        return super.getParameterNames();
    }

    public void removeParameter(String name) {
        super.removeParameter(name);
    }

    public void setParameter(String name, String value) throws ParseException {
        super.setParameter(name, value);
    }

    public String getName() {
        return ReferencesHeader.NAME;
    }

    public StringBuilder encodeBody(StringBuilder buffer) {
        if (this.parameters.isEmpty()) {
            return buffer.append(this.callId);
        }
        return this.parameters.encode(buffer.append(this.callId).append(Separators.SEMICOLON));
    }

    public void setValue(String value) throws ParseException {
        throw new UnsupportedOperationException("operation not supported");
    }
}
