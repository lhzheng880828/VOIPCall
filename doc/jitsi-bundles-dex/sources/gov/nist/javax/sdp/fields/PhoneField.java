package gov.nist.javax.sdp.fields;

import javax.sdp.Phone;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import org.jitsi.gov.nist.core.Separators;

public class PhoneField extends SDPField implements Phone {
    protected String name;
    protected String phoneNumber;

    public PhoneField() {
        super(SDPFieldNames.PHONE_FIELD);
    }

    public String getName() {
        return this.name;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getValue() throws SdpParseException {
        return getName();
    }

    public void setValue(String value) throws SdpException {
        if (value == null) {
            throw new SdpException("The value parameter is null");
        }
        setName(value);
    }

    public String encode() {
        String encoded_string = SDPFieldNames.PHONE_FIELD;
        if (this.name != null) {
            encoded_string = encoded_string + this.name + Separators.LESS_THAN;
        }
        encoded_string = encoded_string + this.phoneNumber;
        if (this.name != null) {
            encoded_string = encoded_string + Separators.GREATER_THAN;
        }
        return encoded_string + Separators.NEWLINE;
    }
}
