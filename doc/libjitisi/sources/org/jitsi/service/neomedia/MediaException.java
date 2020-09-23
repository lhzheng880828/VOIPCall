package org.jitsi.service.neomedia;

public class MediaException extends Exception {
    public static final int GENERAL_ERROR = 1;
    private static final long serialVersionUID = 0;
    private final int errorCode;

    public MediaException(String message) {
        this(message, 1);
    }

    public MediaException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public MediaException(String message, Throwable cause) {
        this(message, 1, cause);
    }

    public MediaException(String message, int errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
