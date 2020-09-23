package org.jitsi.javax.sip;

import java.util.EventObject;

public class DialogTerminatedEvent extends EventObject {
    private Dialog m_dialog = null;

    public DialogTerminatedEvent(Object source, Dialog dialog) {
        super(source);
        this.m_dialog = dialog;
    }

    public Dialog getDialog() {
        return this.m_dialog;
    }
}
