package gov.nist.javax.sdp.fields;

import org.jitsi.gov.nist.core.Separators;

public class EmailAddress extends SDPObject {
    protected String displayName;
    protected Email email;

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public String encode() {
        String encoded_string;
        if (this.displayName != null) {
            encoded_string = this.displayName + Separators.LESS_THAN;
        } else {
            encoded_string = "";
        }
        encoded_string = encoded_string + this.email.encode();
        if (this.displayName != null) {
            return encoded_string + Separators.GREATER_THAN;
        }
        return encoded_string;
    }

    public Object clone() {
        EmailAddress retval = (EmailAddress) super.clone();
        if (this.email != null) {
            retval.email = (Email) this.email.clone();
        }
        return retval;
    }
}
