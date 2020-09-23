package javax.sdp;

import java.util.Vector;

public interface Media extends Field {
    Vector getMediaFormats(boolean z) throws SdpParseException;

    int getMediaPort() throws SdpParseException;

    String getMediaType() throws SdpParseException;

    int getPortCount() throws SdpParseException;

    String getProtocol() throws SdpParseException;

    void setMediaFormats(Vector vector) throws SdpException;

    void setMediaPort(int i) throws SdpException;

    void setMediaType(String str) throws SdpException;

    void setPortCount(int i) throws SdpException;

    void setProtocol(String str) throws SdpException;

    String toString();
}
