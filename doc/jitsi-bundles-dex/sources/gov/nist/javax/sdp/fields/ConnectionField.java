package gov.nist.javax.sdp.fields;

import javax.sdp.Connection;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import org.jitsi.gov.nist.core.Host;
import org.jitsi.gov.nist.core.Separators;

public class ConnectionField extends SDPField implements Connection {
    protected ConnectionAddress address;
    protected String addrtype;
    protected String nettype;

    public ConnectionField() {
        super(SDPFieldNames.CONNECTION_FIELD);
    }

    public String getNettype() {
        return this.nettype;
    }

    public String getAddrtype() {
        return this.addrtype;
    }

    public ConnectionAddress getConnectionAddress() {
        return this.address;
    }

    public void setNettype(String n) {
        this.nettype = n;
    }

    public void setAddrType(String a) {
        this.addrtype = a;
    }

    public void setAddress(ConnectionAddress a) {
        this.address = a;
    }

    public String encode() {
        String encoded_string = SDPFieldNames.CONNECTION_FIELD;
        if (this.nettype != null) {
            encoded_string = encoded_string + this.nettype;
        }
        if (this.addrtype != null) {
            encoded_string = encoded_string + Separators.SP + this.addrtype;
        }
        if (this.address != null) {
            encoded_string = encoded_string + Separators.SP + this.address.encode();
        }
        return encoded_string + Separators.NEWLINE;
    }

    public String toString() {
        return encode();
    }

    public String getAddress() throws SdpParseException {
        ConnectionAddress connectionAddress = getConnectionAddress();
        if (connectionAddress == null) {
            return null;
        }
        Host host = connectionAddress.getAddress();
        if (host != null) {
            return host.getAddress();
        }
        return null;
    }

    public String getAddressType() throws SdpParseException {
        return getAddrtype();
    }

    public String getNetworkType() throws SdpParseException {
        return getNettype();
    }

    public void setAddress(String addr) throws SdpException {
        if (addr == null) {
            throw new SdpException("the addr is null");
        }
        if (this.address == null) {
            this.address = new ConnectionAddress();
            this.address.setAddress(new Host(addr));
        } else {
            Host host = this.address.getAddress();
            if (host == null) {
                this.address.setAddress(new Host(addr));
            } else {
                host.setAddress(addr);
            }
        }
        setAddress(this.address);
    }

    public void setAddressType(String type) throws SdpException {
        if (type == null) {
            throw new SdpException("the type is null");
        }
        this.addrtype = type;
    }

    public void setNetworkType(String type) throws SdpException {
        if (type == null) {
            throw new SdpException("the type is null");
        }
        setNettype(type);
    }

    public Object clone() {
        ConnectionField retval = (ConnectionField) super.clone();
        if (this.address != null) {
            retval.address = (ConnectionAddress) this.address.clone();
        }
        return retval;
    }
}
