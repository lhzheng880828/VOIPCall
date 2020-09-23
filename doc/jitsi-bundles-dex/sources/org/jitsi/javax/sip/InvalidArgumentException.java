package org.jitsi.javax.sip;

public class InvalidArgumentException extends Exception {
    protected Throwable m_Cause = null;

    public InvalidArgumentException(String message) {
        super(message);
    }

    public InvalidArgumentException(String message, Throwable cause) {
        super(message);
        this.m_Cause = cause;
    }

    public Throwable getCause() {
        return this.m_Cause;
    }
}
