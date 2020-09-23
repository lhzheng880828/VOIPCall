package org.jitsi.javax.sip;

public class TransportAlreadySupportedException extends SipException {
    public TransportAlreadySupportedException(String message) {
        super(message);
    }

    public TransportAlreadySupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
