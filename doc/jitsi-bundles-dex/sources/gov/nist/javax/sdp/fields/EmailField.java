package gov.nist.javax.sdp.fields;

import javax.sdp.EMail;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import org.jitsi.gov.nist.core.Separators;

public class EmailField extends SDPField implements EMail {
    protected EmailAddress emailAddress = new EmailAddress();

    public EmailField() {
        super(SDPFieldNames.EMAIL_FIELD);
    }

    public EmailAddress getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(EmailAddress emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String encode() {
        return SDPFieldNames.EMAIL_FIELD + this.emailAddress.encode() + Separators.NEWLINE;
    }

    public String toString() {
        return encode();
    }

    public String getValue() throws SdpParseException {
        if (this.emailAddress == null) {
            return null;
        }
        return this.emailAddress.getDisplayName();
    }

    public void setValue(String value) throws SdpException {
        if (value == null) {
            throw new SdpException("The value is null");
        }
        this.emailAddress.setDisplayName(value);
    }

    public Object clone() {
        EmailField retval = (EmailField) super.clone();
        if (this.emailAddress != null) {
            retval.emailAddress = (EmailAddress) this.emailAddress.clone();
        }
        return retval;
    }
}
