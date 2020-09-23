package net.sf.fmj.media;

import javax.media.Control;
import javax.media.Controls;
import net.sf.fmj.utility.ControlCollection;

public abstract class AbstractControls implements Controls {
    private final ControlCollection controls = new ControlCollection();

    /* access modifiers changed from: protected */
    public void addControl(Control control) {
        this.controls.addControl(control);
    }

    public Object getControl(String controlType) {
        return this.controls.getControl(controlType);
    }

    public Object[] getControls() {
        return this.controls.getControls();
    }

    /* access modifiers changed from: protected */
    public void removeControl(Control control) {
        this.controls.removeControl(control);
    }
}
