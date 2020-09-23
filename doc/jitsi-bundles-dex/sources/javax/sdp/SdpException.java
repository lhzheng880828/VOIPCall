package javax.sdp;

import org.jitsi.gov.nist.core.Separators;

public class SdpException extends Exception {
    public SdpException(String message) {
        super(message);
    }

    public SdpException(String message, Throwable rootCause) {
        super(rootCause.getMessage() + Separators.SEMICOLON + message);
    }

    public SdpException(Throwable rootCause) {
        super(rootCause.getLocalizedMessage());
    }

    public Throwable getRootCause() {
        return fillInStackTrace();
    }
}
