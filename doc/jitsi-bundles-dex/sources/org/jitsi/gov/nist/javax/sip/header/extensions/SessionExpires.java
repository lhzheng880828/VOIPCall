package org.jitsi.gov.nist.javax.sip.header.extensions;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.ParametersHeader;
import org.jitsi.javax.sip.InvalidArgumentException;
import org.jitsi.javax.sip.header.ExtensionHeader;

public final class SessionExpires extends ParametersHeader implements ExtensionHeader, SessionExpiresHeader {
    public static final String NAME = "Session-Expires";
    public static final String REFRESHER = "refresher";
    private static final long serialVersionUID = 8765762413224043300L;
    public int expires;

    public SessionExpires() {
        super("Session-Expires");
    }

    public int getExpires() {
        return this.expires;
    }

    public void setExpires(int expires) throws InvalidArgumentException {
        if (expires < 0) {
            throw new InvalidArgumentException("bad argument " + expires);
        }
        this.expires = expires;
    }

    public void setValue(String value) throws ParseException {
        throw new ParseException(value, 0);
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder retval) {
        retval.append(Integer.toString(this.expires));
        if (!this.parameters.isEmpty()) {
            retval.append(Separators.SEMICOLON);
            this.parameters.encode(retval);
        }
        return retval;
    }

    public String getRefresher() {
        return this.parameters.getParameter(REFRESHER);
    }

    public void setRefresher(String refresher) {
        this.parameters.set(REFRESHER, refresher);
    }
}
