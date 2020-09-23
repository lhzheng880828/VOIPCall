package net.sf.fmj.media.control;

import javax.media.Control;

public class ControlChangeEvent {
    private Control c;

    public ControlChangeEvent(Control c) {
        this.c = c;
    }

    public Control getControl() {
        return this.c;
    }
}
