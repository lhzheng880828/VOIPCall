package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.address.GenericURI;
import org.jitsi.javax.sip.address.URI;
import org.jitsi.javax.sip.header.ErrorInfoHeader;

public final class ErrorInfo extends ParametersHeader implements ErrorInfoHeader {
    private static final long serialVersionUID = -6347702901964436362L;
    protected GenericURI errorInfo;

    public ErrorInfo() {
        super("Error-Info");
    }

    public ErrorInfo(GenericURI errorInfo) {
        this();
        this.errorInfo = errorInfo;
    }

    public StringBuilder encodeBody(StringBuilder retval) {
        retval.append(Separators.LESS_THAN);
        this.errorInfo.encode(retval);
        retval.append(Separators.GREATER_THAN);
        if (!this.parameters.isEmpty()) {
            retval.append(Separators.SEMICOLON);
            this.parameters.encode(retval);
        }
        return retval;
    }

    public void setErrorInfo(URI errorInfo) {
        this.errorInfo = (GenericURI) errorInfo;
    }

    public URI getErrorInfo() {
        return this.errorInfo;
    }

    public void setErrorMessage(String message) throws ParseException {
        if (message == null) {
            throw new NullPointerException("JAIN-SIP Exception , ErrorInfoHeader, setErrorMessage(), the message parameter is null");
        }
        setParameter("message", message);
    }

    public String getErrorMessage() {
        return getParameter("message");
    }

    public Object clone() {
        ErrorInfo retval = (ErrorInfo) super.clone();
        if (this.errorInfo != null) {
            retval.errorInfo = (GenericURI) this.errorInfo.clone();
        }
        return retval;
    }
}
