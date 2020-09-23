package javax.sdp;

public interface Phone extends Field {
    String getValue() throws SdpParseException;

    void setValue(String str) throws SdpException;
}
