package org.jitsi.javax.sip;

public class SipException extends Exception {
    protected Throwable m_Cause = null;

    public SipException(String message) {
        super(message);
    }

    public SipException(String message, Throwable cause) {
        super(message);
        this.m_Cause = cause;
    }

    public Throwable getCause() {
        return this.m_Cause;
    }
}
