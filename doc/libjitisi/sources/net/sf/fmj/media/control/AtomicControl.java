package net.sf.fmj.media.control;

import javax.media.Control;

public interface AtomicControl extends Control {
    void addControlChangeListener(ControlChangeListener controlChangeListener);

    boolean getEnabled();

    Control getParent();

    String getTip();

    boolean getVisible();

    boolean isDefault();

    boolean isReadOnly();

    void removeControlChangeListener(ControlChangeListener controlChangeListener);

    void setEnabled(boolean z);

    void setTip(String str);

    void setVisible(boolean z);
}
