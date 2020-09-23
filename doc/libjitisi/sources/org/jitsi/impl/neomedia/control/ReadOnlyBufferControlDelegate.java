package org.jitsi.impl.neomedia.control;

import javax.media.control.BufferControl;
import org.jitsi.android.util.java.awt.Component;

public class ReadOnlyBufferControlDelegate implements BufferControl {
    private final BufferControl bufferControl;

    public ReadOnlyBufferControlDelegate(BufferControl bufferControl) {
        this.bufferControl = bufferControl;
    }

    public long getBufferLength() {
        return this.bufferControl.getBufferLength();
    }

    public Component getControlComponent() {
        return this.bufferControl.getControlComponent();
    }

    public boolean getEnabledThreshold() {
        return this.bufferControl.getEnabledThreshold();
    }

    public long getMinimumThreshold() {
        return this.bufferControl.getMinimumThreshold();
    }

    public long setBufferLength(long bufferLength) {
        return getBufferLength();
    }

    public void setEnabledThreshold(boolean enabledThreshold) {
    }

    public long setMinimumThreshold(long minimumThreshold) {
        return getMinimumThreshold();
    }
}
