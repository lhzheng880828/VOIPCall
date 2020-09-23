package gov.nist.javax.sdp.fields;

import org.jitsi.gov.nist.core.Host;
import org.jitsi.gov.nist.core.Separators;

public class ConnectionAddress extends SDPObject {
    protected Host address;
    protected int port;
    protected int ttl;

    public Host getAddress() {
        return this.address;
    }

    public int getTtl() {
        return this.ttl;
    }

    public int getPort() {
        return this.port;
    }

    public void setAddress(Host a) {
        this.address = a;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public void setPort(int p) {
        this.port = p;
    }

    public String encode() {
        String encoded_string = "";
        if (this.address != null) {
            encoded_string = this.address.encode();
            if (Host.isIPv6Reference(encoded_string)) {
                encoded_string = encoded_string.substring(1, encoded_string.length() - 1);
            }
        }
        if (this.ttl != 0 && this.port != 0) {
            return encoded_string + Separators.SLASH + this.ttl + Separators.SLASH + this.port;
        }
        if (this.ttl != 0) {
            return encoded_string + Separators.SLASH + this.ttl;
        }
        return encoded_string;
    }

    public Object clone() {
        ConnectionAddress retval = (ConnectionAddress) super.clone();
        if (this.address != null) {
            retval.address = (Host) this.address.clone();
        }
        return retval;
    }
}
