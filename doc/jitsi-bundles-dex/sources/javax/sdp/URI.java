package javax.sdp;

import java.net.URL;

public interface URI extends Field {
    URL get() throws SdpParseException;

    void set(URL url) throws SdpException;
}
