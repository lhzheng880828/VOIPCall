package net.sf.fmj.media.control;

import java.util.Vector;
import javax.media.Control;
import org.jitsi.android.util.java.awt.Component;

public class AtomicControlAdapter implements AtomicControl {
    protected Component component = null;
    protected boolean enabled = true;
    protected boolean isdefault = false;
    private Vector listeners = null;
    protected Control parent = null;

    public AtomicControlAdapter(Component c, boolean def, Control parent) {
        this.component = c;
        this.isdefault = def;
        this.parent = parent;
    }

    public void addControlChangeListener(ControlChangeListener ccl) {
        if (this.listeners == null) {
            this.listeners = new Vector();
        }
        if (ccl != null) {
            this.listeners.addElement(ccl);
        }
    }

    public Component getControlComponent() {
        return this.component;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public Control getParent() {
        return this.parent;
    }

    public String getTip() {
        return null;
    }

    public boolean getVisible() {
        return true;
    }

    public void informListeners() {
        if (this.listeners != null) {
            for (int i = 0; i < this.listeners.size(); i++) {
                ((ControlChangeListener) this.listeners.elementAt(i)).controlChanged(new ControlChangeEvent(this));
            }
        }
    }

    public boolean isDefault() {
        return this.isdefault;
    }

    public boolean isReadOnly() {
        return false;
    }

    public void removeControlChangeListener(ControlChangeListener ccl) {
        if (this.listeners != null && ccl != null) {
            this.listeners.removeElement(ccl);
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (this.component != null) {
            this.component.setEnabled(enabled);
        }
        informListeners();
    }

    public void setParent(Control p) {
        this.parent = p;
    }

    public void setTip(String tip) {
    }

    public void setVisible(boolean visible) {
    }
}
