package gov.nist.javax.sdp.fields;

import javax.sdp.Key;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import org.jitsi.gov.nist.core.Separators;

public class KeyField extends SDPField implements Key {
    protected String keyData;
    protected String type;

    public KeyField() {
        super(SDPFieldNames.KEY_FIELD);
    }

    public String getType() {
        return this.type;
    }

    public String getKeyData() {
        return this.keyData;
    }

    public void setType(String t) {
        this.type = t;
    }

    public void setKeyData(String k) {
        this.keyData = k;
    }

    public String encode() {
        String encoded_string = SDPFieldNames.KEY_FIELD + this.type;
        if (this.keyData != null) {
            encoded_string = (encoded_string + Separators.COLON) + this.keyData;
        }
        return encoded_string + Separators.NEWLINE;
    }

    public String getMethod() throws SdpParseException {
        return this.type;
    }

    public void setMethod(String name) throws SdpException {
        this.type = name;
    }

    public boolean hasKey() throws SdpParseException {
        return getKeyData() != null;
    }

    public String getKey() throws SdpParseException {
        return getKeyData();
    }

    public void setKey(String key) throws SdpException {
        if (key == null) {
            throw new SdpException("The key is null");
        }
        setKeyData(key);
    }
}
