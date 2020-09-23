package net.java.sip.communicator.impl.protocol.sip.xcap;

public class XCapException extends Exception {
    private static final long serialVersionUID = 0;

    public XCapException(String message) {
        super(message);
    }

    public XCapException(String message, Throwable cause) {
        super(message, cause);
    }

    public XCapException(Throwable cause) {
        super(cause);
    }
}
