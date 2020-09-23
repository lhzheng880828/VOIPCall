package org.jitsi.impl.neomedia.control;

import javax.media.control.FrameRateControl;
import org.jitsi.android.util.java.awt.Component;

public class FrameRateControlAdapter implements FrameRateControl {
    public Component getControlComponent() {
        return null;
    }

    public float getFrameRate() {
        return -1.0f;
    }

    public float getMaxSupportedFrameRate() {
        return -1.0f;
    }

    public float getPreferredFrameRate() {
        return -1.0f;
    }

    public float setFrameRate(float frameRate) {
        return -1.0f;
    }
}
