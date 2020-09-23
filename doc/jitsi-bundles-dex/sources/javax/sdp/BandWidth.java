package javax.sdp;

public interface BandWidth extends Field {
    public static final String AS = "AS";
    public static final String CT = "CT";

    String getType() throws SdpParseException;

    int getValue() throws SdpParseException;

    void setType(String str) throws SdpException;

    void setValue(int i) throws SdpException;
}
