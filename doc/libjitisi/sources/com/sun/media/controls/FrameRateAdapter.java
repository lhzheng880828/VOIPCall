package com.sun.media.controls;

import com.sun.media.ui.TextComp;
import javax.media.control.FrameRateControl;
import org.jitsi.android.util.java.awt.Component;

public class FrameRateAdapter implements FrameRateControl {
    protected float max;
    protected float min;
    protected Object owner;
    protected boolean settable;
    protected final TextComp textComp = new TextComp();
    protected float value;

    public FrameRateAdapter(float initialFrameRate, float minFrameRate, float maxFrameRate, boolean settable) {
        this.value = initialFrameRate;
        this.min = minFrameRate;
        this.max = maxFrameRate;
        this.settable = settable;
    }

    public FrameRateAdapter(Object owner, float initialFrameRate, float minFrameRate, float maxFrameRate, boolean settable) {
        this.owner = owner;
        this.value = initialFrameRate;
        this.min = minFrameRate;
        this.max = maxFrameRate;
        this.settable = settable;
    }

    public Component getControlComponent() {
        throw new UnsupportedOperationException();
    }

    public float getFrameRate() {
        return this.value;
    }

    public float getMaxSupportedFrameRate() {
        return this.max;
    }

    public Object getOwner() {
        return this.owner;
    }

    public float getPreferredFrameRate() {
        throw new UnsupportedOperationException();
    }

    public float setFrameRate(float newFrameRate) {
        throw new UnsupportedOperationException();
    }

    public void setOwner(Object owner) {
        this.owner = owner;
    }
}
