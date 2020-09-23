package gov.nist.javax.sdp.fields;

import javax.sdp.BandWidth;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import org.jitsi.gov.nist.core.Separators;

public class BandwidthField extends SDPField implements BandWidth {
    protected int bandwidth;
    protected String bwtype;

    public BandwidthField() {
        super(SDPFieldNames.BANDWIDTH_FIELD);
    }

    public String getBwtype() {
        return this.bwtype;
    }

    public int getBandwidth() {
        return this.bandwidth;
    }

    public void setBwtype(String b) {
        this.bwtype = b;
    }

    public void setBandwidth(int b) {
        this.bandwidth = b;
    }

    public String encode() {
        String encoded_string = SDPFieldNames.BANDWIDTH_FIELD;
        if (this.bwtype != null) {
            encoded_string = encoded_string + this.bwtype + Separators.COLON;
        }
        return encoded_string + this.bandwidth + Separators.NEWLINE;
    }

    public String getType() throws SdpParseException {
        return getBwtype();
    }

    public void setType(String type) throws SdpException {
        if (type == null) {
            throw new SdpException("The type is null");
        }
        setBwtype(type);
    }

    public int getValue() throws SdpParseException {
        return getBandwidth();
    }

    public void setValue(int value) throws SdpException {
        setBandwidth(value);
    }
}
