package javax.sdp;

public interface Origin extends Field {
    String getAddress() throws SdpParseException;

    String getAddressType() throws SdpParseException;

    String getNetworkType() throws SdpParseException;

    long getSessionId() throws SdpParseException;

    long getSessionVersion() throws SdpParseException;

    String getUsername() throws SdpParseException;

    void setAddress(String str) throws SdpException;

    void setAddressType(String str) throws SdpException;

    void setNetworkType(String str) throws SdpException;

    void setSessionId(long j) throws SdpException;

    void setSessionVersion(long j) throws SdpException;

    void setUsername(String str) throws SdpException;
}
