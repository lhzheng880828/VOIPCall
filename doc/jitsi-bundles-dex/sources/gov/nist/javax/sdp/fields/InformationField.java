package gov.nist.javax.sdp.fields;

import javax.sdp.Info;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import org.jitsi.gov.nist.core.Separators;

public class InformationField extends SDPField implements Info {
    protected String information;

    public InformationField() {
        super(SDPFieldNames.INFORMATION_FIELD);
    }

    public String getInformation() {
        return this.information;
    }

    public void setInformation(String info) {
        this.information = info;
    }

    public String encode() {
        return SDPFieldNames.INFORMATION_FIELD + this.information + Separators.NEWLINE;
    }

    public String getValue() throws SdpParseException {
        return this.information;
    }

    public void setValue(String value) throws SdpException {
        if (value == null) {
            throw new SdpException("The value is null");
        }
        setInformation(value);
    }
}
