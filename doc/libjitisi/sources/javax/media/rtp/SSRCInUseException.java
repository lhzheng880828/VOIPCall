package javax.media.rtp;

public class SSRCInUseException extends SessionManagerException {
    public SSRCInUseException(String message) {
        super(message);
    }

    public SSRCInUseException(String message, Throwable cause) {
        super(message, cause);
    }

    public SSRCInUseException(Throwable cause) {
        super(cause);
    }
}
