package org.jitsi.gov.nist.javax.sip.header;

import java.text.ParseException;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.javax.sip.header.ContentDispositionHeader;

public final class ContentDisposition extends ParametersHeader implements ContentDispositionHeader {
    private static final long serialVersionUID = 835596496276127003L;
    protected String dispositionType;

    public ContentDisposition() {
        super("Content-Disposition");
    }

    public StringBuilder encodeBody(StringBuilder encoding) {
        encoding.append(this.dispositionType);
        if (!this.parameters.isEmpty()) {
            encoding.append(Separators.SEMICOLON).append(this.parameters.encode());
        }
        return encoding;
    }

    public void setDispositionType(String dispositionType) throws ParseException {
        if (dispositionType == null) {
            throw new NullPointerException("JAIN-SIP Exception, ContentDisposition, setDispositionType(), the dispositionType parameter is null");
        }
        this.dispositionType = dispositionType;
    }

    public String getDispositionType() {
        return this.dispositionType;
    }

    public String getHandling() {
        return getParameter(ParameterNames.HANDLING);
    }

    public void setHandling(String handling) throws ParseException {
        if (handling == null) {
            throw new NullPointerException("JAIN-SIP Exception, ContentDisposition, setHandling(), the handling parameter is null");
        }
        setParameter(ParameterNames.HANDLING, handling);
    }

    public String getContentDisposition() {
        return encodeBody(new StringBuilder()).toString();
    }
}
