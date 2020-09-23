package net.java.sip.communicator.impl.protocol.sip.xcap.model;

public class ParsingException extends Exception {
    private static final long serialVersionUID = 0;

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParsingException(Throwable cause) {
        super(cause);
    }
}
