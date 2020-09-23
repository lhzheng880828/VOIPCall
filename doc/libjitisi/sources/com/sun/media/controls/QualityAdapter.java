package com.sun.media.controls;

import javax.media.control.QualityControl;
import org.jitsi.android.util.java.awt.Component;

public class QualityAdapter implements QualityControl {
    protected boolean isTSsupported;
    protected float maxValue;
    protected float minValue;
    protected float preferredValue;
    protected boolean settable;
    protected float value;

    public QualityAdapter(float value, float value2, float value3, boolean settable) {
        this.preferredValue = value;
        this.minValue = value2;
        this.maxValue = value3;
        this.settable = settable;
    }

    public QualityAdapter(float value, float value2, float value3, boolean ssupported, boolean settable) {
        this.preferredValue = value;
        this.minValue = value2;
        this.maxValue = value3;
        this.isTSsupported = ssupported;
        this.settable = settable;
    }

    public Component getControlComponent() {
        throw new UnsupportedOperationException();
    }

    public float getPreferredQuality() {
        throw new UnsupportedOperationException();
    }

    public float getQuality() {
        throw new UnsupportedOperationException();
    }

    public boolean isTemporalSpatialTradeoffSupported() {
        throw new UnsupportedOperationException();
    }

    public float setQuality(float newQuality) {
        throw new UnsupportedOperationException();
    }
}
