package org.jitsi.javax.sip;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class DialogState implements Serializable {
    public static final DialogState COMPLETED = new DialogState(2);
    public static final DialogState CONFIRMED = new DialogState(1);
    public static final DialogState EARLY = new DialogState(0);
    public static final DialogState TERMINATED = new DialogState(3);
    public static final int _COMPLETED = 2;
    public static final int _CONFIRMED = 1;
    public static final int _EARLY = 0;
    public static final int _TERMINATED = 3;
    private static DialogState[] m_dialogStateArray = new DialogState[m_size];
    private static int m_size = 4;
    private int m_dialogState;

    private DialogState(int dialogState) {
        this.m_dialogState = dialogState;
        m_dialogStateArray[this.m_dialogState] = this;
    }

    public static DialogState getObject(int dialogState) {
        if (dialogState >= 0 && dialogState < m_size) {
            return m_dialogStateArray[dialogState];
        }
        throw new IllegalArgumentException("Invalid dialogState value");
    }

    public int getValue() {
        return this.m_dialogState;
    }

    private Object readResolve() throws ObjectStreamException {
        return m_dialogStateArray[this.m_dialogState];
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if ((obj instanceof DialogState) && ((DialogState) obj).m_dialogState == this.m_dialogState) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.m_dialogState;
    }

    public String toString() {
        String text = "";
        switch (this.m_dialogState) {
            case 0:
                return "Early Dialog";
            case 1:
                return "Confirmed Dialog";
            case 2:
                return "Completed Dialog";
            case 3:
                return "Terminated Dialog";
            default:
                return "Error while printing Dialog State";
        }
    }
}
