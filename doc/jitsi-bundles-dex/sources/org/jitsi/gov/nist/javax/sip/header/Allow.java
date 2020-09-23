package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.javax.sip.header.AllowHeader;

public final class Allow extends SIPHeader implements AllowHeader {
    private static final long serialVersionUID = -3105079479020693930L;
    protected String method;

    public Allow() {
        super("Allow");
    }

    public Allow(String m) {
        super("Allow");
        this.method = m;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) throws ParseException {
        if (method == null) {
            throw new NullPointerException("JAIN-SIP Exception, Allow, setMethod(), the method parameter is null.");
        }
        this.method = method;
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder buffer) {
        return buffer.append(this.method);
    }
}
