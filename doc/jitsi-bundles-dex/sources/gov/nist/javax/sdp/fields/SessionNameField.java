package gov.nist.javax.sdp.fields;

import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sdp.SessionName;
import org.jitsi.gov.nist.core.Separators;

public class SessionNameField extends SDPField implements SessionName {
    protected String sessionName;

    public SessionNameField() {
        super(SDPFieldNames.SESSION_NAME_FIELD);
    }

    public String getSessionName() {
        return this.sessionName;
    }

    public void setSessionName(String s) {
        this.sessionName = s;
    }

    public String getValue() throws SdpParseException {
        return getSessionName();
    }

    public void setValue(String value) throws SdpException {
        if (value == null) {
            throw new SdpException("The value is null");
        }
        setSessionName(value);
    }

    public String encode() {
        return SDPFieldNames.SESSION_NAME_FIELD + this.sessionName + Separators.NEWLINE;
    }
}
