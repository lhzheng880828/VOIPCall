package org.jitsi.javax.sip.header;

import org.jitsi.javax.sip.SipException;

public class TooManyHopsException extends SipException {
    public TooManyHopsException(String message) {
        super(message);
    }

    public TooManyHopsException(String message, Throwable cause) {
        super(message, cause);
    }
}
