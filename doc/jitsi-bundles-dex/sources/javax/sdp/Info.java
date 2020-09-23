package javax.sdp;

public interface Info extends Field {
    String getValue() throws SdpParseException;

    void setValue(String str) throws SdpException;
}
