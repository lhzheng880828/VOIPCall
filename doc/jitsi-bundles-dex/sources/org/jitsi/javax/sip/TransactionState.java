package org.jitsi.javax.sip;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class TransactionState implements Serializable {
    public static final TransactionState CALLING = new TransactionState(0);
    public static final TransactionState COMPLETED = new TransactionState(3);
    public static final TransactionState CONFIRMED = new TransactionState(4);
    public static final TransactionState PROCEEDING = new TransactionState(2);
    public static final TransactionState TERMINATED = new TransactionState(5);
    public static final TransactionState TRYING = new TransactionState(1);
    public static final int _CALLING = 0;
    public static final int _COMPLETED = 3;
    public static final int _CONFIRMED = 4;
    public static final int _PROCEEDING = 2;
    public static final int _TERMINATED = 5;
    public static final int _TRYING = 1;
    private static int m_size = 6;
    private static TransactionState[] m_transStateArray = new TransactionState[m_size];
    private int m_transactionState;

    private TransactionState(int transactionState) {
        this.m_transactionState = transactionState;
        m_transStateArray[this.m_transactionState] = this;
    }

    public static TransactionState getObject(int transactionState) {
        if (transactionState >= 0 && transactionState < m_size) {
            return m_transStateArray[transactionState];
        }
        throw new IllegalArgumentException("Invalid transactionState value");
    }

    public int getValue() {
        return this.m_transactionState;
    }

    private Object readResolve() throws ObjectStreamException {
        return m_transStateArray[this.m_transactionState];
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj instanceof TransactionState) && ((TransactionState) obj).m_transactionState == this.m_transactionState) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.m_transactionState;
    }

    public String toString() {
        String text = "";
        switch (this.m_transactionState) {
            case 0:
                return "Calling Transaction";
            case 1:
                return "Trying Transaction";
            case 2:
                return "Proceeding Transaction";
            case 3:
                return "Completed Transaction";
            case 4:
                return "Confirmed Transaction";
            case 5:
                return "Terminated Transaction";
            default:
                return "Error while printing Transaction State";
        }
    }
}
