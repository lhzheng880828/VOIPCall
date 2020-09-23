package javax.sdp;

public interface Attribute extends Field {
    String getName() throws SdpParseException;

    String getValue() throws SdpParseException;

    boolean hasValue() throws SdpParseException;

    void setName(String str) throws SdpException;

    void setValue(String str) throws SdpException;
}
