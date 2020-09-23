package net.sf.fmj.utility;

import java.util.Vector;
import javax.media.Control;

public class ControlCollection {
    private static final Control[] CONTROL_SPEC = new Control[0];
    private Vector controls = new Vector();

    public void addControl(Control control) {
        synchronized (this.controls) {
            this.controls.add(control);
        }
    }

    public void clear() {
        synchronized (this.controls) {
            this.controls.clear();
        }
    }

    public Control getControl(String controlType) {
        try {
            Class<?> cls = Class.forName(controlType);
            synchronized (this.controls) {
                Control[] cs = getControls();
                for (int i = 0; i < cs.length; i++) {
                    if (cls.isInstance(cs[i])) {
                        Control control = cs[i];
                        return control;
                    }
                }
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public Control[] getControls() {
        Control[] controlArr;
        synchronized (this.controls) {
            controlArr = (Control[]) this.controls.toArray(CONTROL_SPEC);
        }
        return controlArr;
    }

    public void removeControl(Control control) {
        synchronized (this.controls) {
            this.controls.remove(control);
        }
    }
}
