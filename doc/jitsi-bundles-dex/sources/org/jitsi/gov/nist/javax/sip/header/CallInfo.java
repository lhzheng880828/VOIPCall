package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.GenericURI;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.CallInfoHeader;

public final class CallInfo extends ParametersHeader implements CallInfoHeader {
    private static final long serialVersionUID = -8179246487696752928L;
    protected GenericURI info;

    public CallInfo() {
        super("Call-Info");
    }

    public String encodeBody() {
        return encodeBody(new StringBuilder()).toString();
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder buffer) {
        buffer.append(Separators.LESS_THAN);
        this.info.encode(buffer);
        buffer.append(Separators.GREATER_THAN);
        if (!(this.parameters == null || this.parameters.isEmpty())) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }

    public String getPurpose() {
        return getParameter("purpose");
    }

    public URI getInfo() {
        return this.info;
    }

    public void setPurpose(String purpose) {
        if (purpose == null) {
            throw new NullPointerException("null arg");
        }
        try {
            setParameter("purpose", purpose);
        } catch (ParseException e) {
        }
    }

    public void setInfo(URI info) {
        this.info = (GenericURI) info;
    }

    public Object clone() {
        CallInfo retval = (CallInfo) super.clone();
        if (this.info != null) {
            retval.info = (GenericURI) this.info.clone();
        }
        return retval;
    }
}
