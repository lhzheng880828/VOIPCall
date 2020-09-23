package gov.nist.javax.sdp.fields;

import javax.sdp.Origin;
import javax.sdp.SdpException;
import javax.sdp.SdpParseException;
import org.jitsi.gov.nist.core.Host;
import org.jitsi.gov.nist.core.Separators;

public class OriginField extends SDPField implements Origin {
    protected Host address;
    protected String addrtype;
    protected String nettype;
    private String sessIdString;
    private String sessVersionString;
    protected String username;

    public OriginField() {
        super(SDPFieldNames.ORIGIN_FIELD);
    }

    public String getUsername() throws SdpParseException {
        return this.username;
    }

    public long getSessId() {
        return Long.valueOf(this.sessIdString).longValue();
    }

    public String getSessIdAsString() {
        return this.sessIdString;
    }

    public long getSessVersion() {
        return Long.valueOf(this.sessVersionString).longValue();
    }

    public String getSessVersionAsString() {
        return this.sessVersionString;
    }

    public String getNettype() {
        return this.nettype;
    }

    public String getAddrtype() {
        return this.addrtype;
    }

    public Host getHost() {
        return this.address;
    }

    public void setSessId(long s) {
        this.sessIdString = Long.toString(s);
    }

    public void setSessionId(String sessId) {
        this.sessIdString = sessId;
    }

    public void setSessVersion(long s) {
        this.sessVersionString = Long.toString(s);
    }

    public void setSessVersion(String s) {
        this.sessVersionString = s;
    }

    public void setNettype(String n) {
        this.nettype = n;
    }

    public void setAddrtype(String a) {
        this.addrtype = a;
    }

    public void setAddress(Host a) {
        this.address = a;
    }

    public void setUsername(String user) throws SdpException {
        if (user == null) {
            throw new SdpException("The user parameter is null");
        }
        this.username = user;
    }

    public long getSessionId() throws SdpParseException {
        return getSessId();
    }

    public void setSessionId(long id) throws SdpException {
        if (id < 0) {
            throw new SdpException("The is parameter is <0");
        }
        setSessId(id);
    }

    public long getSessionVersion() throws SdpParseException {
        return getSessVersion();
    }

    public void setSessionVersion(long version) throws SdpException {
        if (version < 0) {
            throw new SdpException("The version parameter is <0");
        }
        setSessVersion(version);
    }

    public String getAddress() throws SdpParseException {
        Host addr = getHost();
        if (addr == null) {
            return null;
        }
        return addr.getAddress();
    }

    public String getAddressType() throws SdpParseException {
        return getAddrtype();
    }

    public String getNetworkType() throws SdpParseException {
        return getNettype();
    }

    public void setAddress(String addr) throws SdpException {
        if (addr == null) {
            throw new SdpException("The addr parameter is null");
        }
        Host host = getHost();
        if (host == null) {
            host = new Host();
        }
        host.setAddress(addr);
        setAddress(host);
    }

    public void setAddressType(String type) throws SdpException {
        if (type == null) {
            throw new SdpException("The type parameter is <0");
        }
        setAddrtype(type);
    }

    public void setNetworkType(String type) throws SdpException {
        if (type == null) {
            throw new SdpException("The type parameter is <0");
        }
        setNettype(type);
    }

    public String encode() {
        String addressStr = null;
        if (this.address != null) {
            addressStr = this.address.encode();
            if (Host.isIPv6Reference(addressStr)) {
                addressStr = addressStr.substring(1, addressStr.length() - 1);
            }
        }
        return SDPFieldNames.ORIGIN_FIELD + this.username + Separators.SP + this.sessIdString + Separators.SP + this.sessVersionString + Separators.SP + this.nettype + Separators.SP + this.addrtype + Separators.SP + addressStr + Separators.NEWLINE;
    }

    public Object clone() {
        OriginField retval = (OriginField) super.clone();
        if (this.address != null) {
            retval.address = (Host) this.address.clone();
        }
        return retval;
    }
}
