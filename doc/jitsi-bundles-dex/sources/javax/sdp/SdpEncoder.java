package javax.sdp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public interface SdpEncoder {
    void output(SessionDescription sessionDescription, OutputStream outputStream) throws IOException;

    void setEncoding(String str) throws UnsupportedEncodingException;

    void setRtpmapAttribute(boolean z);

    void setTypedTime(boolean z);
}
