package javax.sdp;

public interface Connection extends Field {
    public static final String IN = "IN";
    public static final String IP4 = "IP4";
    public static final String IP6 = "IP6";

    String getAddress() throws SdpParseException;

    String getAddressType() throws SdpParseException;

    String getNetworkType() throws SdpParseException;

    void setAddress(String str) throws SdpException;

    void setAddressType(String str) throws SdpException;

    void setNetworkType(String str) throws SdpException;
}
