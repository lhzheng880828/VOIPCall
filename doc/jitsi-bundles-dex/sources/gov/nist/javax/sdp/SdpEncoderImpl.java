package gov.nist.javax.sdp;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.sdp.SessionDescription;

public class SdpEncoderImpl {
    public void setEncoding(String enc) throws UnsupportedEncodingException {
        throw new UnsupportedEncodingException("Method not supported");
    }

    public void setTypedTime(boolean flag) {
    }

    public void setRtpmapAttribute(boolean flag) {
    }

    public void output(SessionDescription sd, OutputStream out) throws IOException {
        if (out instanceof ObjectOutputStream) {
            ObjectOutputStream output = (ObjectOutputStream) out;
            if (sd != null) {
                output.writeObject(sd);
                return;
            }
            throw new IOException("The parameter is null");
        }
        throw new IOException("The output stream has to be an instance of ObjectOutputStream");
    }
}
