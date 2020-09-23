package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.GenericURI;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.AlertInfoHeader;

public final class AlertInfo extends ParametersHeader implements AlertInfoHeader {
    private static final long serialVersionUID = 4159657362051508719L;
    protected String string;
    protected GenericURI uri;

    public AlertInfo() {
        super("Alert-Info");
    }

    /* access modifiers changed from: protected */
    public StringBuilder encodeBody(StringBuilder encoding) {
        if (this.uri != null) {
            encoding.append(Separators.LESS_THAN).append(this.uri.encode()).append(Separators.GREATER_THAN);
        } else if (this.string != null) {
            encoding.append(this.string);
        }
        if (!this.parameters.isEmpty()) {
            encoding.append(Separators.SEMICOLON).append(this.parameters.encode());
        }
        return encoding;
    }

    public void setAlertInfo(URI uri) {
        this.uri = (GenericURI) uri;
    }

    public void setAlertInfo(String string) {
        this.string = string;
    }

    public URI getAlertInfo() {
        if (this.uri != null) {
            return this.uri;
        }
        try {
            return new GenericURI(this.string);
        } catch (ParseException e) {
            return null;
        }
    }

    public Object clone() {
        AlertInfo retval = (AlertInfo) super.clone();
        if (this.uri != null) {
            retval.uri = (GenericURI) this.uri.clone();
        } else if (this.string != null) {
            retval.string = this.string;
        }
        return retval;
    }
}
