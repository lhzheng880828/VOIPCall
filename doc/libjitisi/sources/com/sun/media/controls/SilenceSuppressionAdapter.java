package com.sun.media.controls;

import javax.media.Codec;
import javax.media.control.SilenceSuppressionControl;
import org.jitsi.android.util.java.awt.Component;

public class SilenceSuppressionAdapter implements SilenceSuppressionControl {
    String CONTROL_STRING;
    Component component;
    protected boolean isSetable;
    protected Codec owner;
    protected boolean silenceSuppression;

    public SilenceSuppressionAdapter(Codec owner, boolean silenceSuppression, boolean isSetable) {
        this.owner = owner;
        this.silenceSuppression = silenceSuppression;
        this.isSetable = isSetable;
    }

    public Component getControlComponent() {
        return this.component;
    }

    public boolean getSilenceSuppression() {
        return this.silenceSuppression;
    }

    public boolean isSilenceSuppressionSupported() {
        return this.isSetable;
    }

    public boolean setSilenceSuppression(boolean silenceSuppression) {
        if (this.isSetable) {
            this.silenceSuppression = silenceSuppression;
        }
        return this.silenceSuppression;
    }
}
