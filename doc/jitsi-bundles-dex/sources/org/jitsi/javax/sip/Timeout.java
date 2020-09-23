package org.jitsi.javax.sip;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class Timeout implements Serializable {
    public static final Timeout RETRANSMIT = new Timeout(0);
    public static final Timeout TRANSACTION = new Timeout(1);
    public static final int _RETRANSMIT = 0;
    public static final int _TRANSACTION = 1;
    private static int m_size = 2;
    private static Timeout[] m_timeoutArray = new Timeout[m_size];
    private int m_timeout;

    private Timeout(int timeout) {
        this.m_timeout = timeout;
        m_timeoutArray[this.m_timeout] = this;
    }

    public Timeout getObject(int timeout) {
        if (timeout >= 0 && timeout < m_size) {
            return m_timeoutArray[timeout];
        }
        throw new IllegalArgumentException("Invalid timeout value");
    }

    public int getValue() {
        return this.m_timeout;
    }

    private Object readResolve() throws ObjectStreamException {
        return m_timeoutArray[this.m_timeout];
    }

    public String toString() {
        String text = "";
        switch (this.m_timeout) {
            case 0:
                return "Retransmission Timeout";
            case 1:
                return "Transaction Timeout";
            default:
                return "Error while printing Timeout";
        }
    }
}
