package gov.nist.javax.sdp.fields;

import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import javax.sdp.Version;
import org.jitsi.gov.nist.core.Separators;

public class ProtoVersionField extends SDPField implements Version {
    protected int protoVersion;

    public ProtoVersionField() {
        super(SDPFieldNames.PROTO_VERSION_FIELD);
    }

    public int getProtoVersion() {
        return this.protoVersion;
    }

    public void setProtoVersion(int pv) {
        this.protoVersion = pv;
    }

    public int getVersion() throws SdpParseException {
        return getProtoVersion();
    }

    public void setVersion(int value) throws SdpException {
        if (value < 0) {
            throw new SdpException("The value is <0");
        }
        setProtoVersion(value);
    }

    public String encode() {
        return SDPFieldNames.PROTO_VERSION_FIELD + this.protoVersion + Separators.NEWLINE;
    }
}
