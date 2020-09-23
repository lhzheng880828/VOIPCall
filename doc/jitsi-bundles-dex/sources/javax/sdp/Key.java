package javax.sdp;

public interface Key extends Field {
    String getKey() throws SdpParseException;

    String getMethod() throws SdpParseException;

    boolean hasKey() throws SdpParseException;

    void setKey(String str) throws SdpException;

    void setMethod(String str) throws SdpException;
}
