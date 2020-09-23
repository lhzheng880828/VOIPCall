package javax.media.rtp;

public class SessionManagerException extends Exception {
    public SessionManagerException(String message) {
        super(message);
    }

    public SessionManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionManagerException(Throwable cause) {
        super(cause);
    }
}
