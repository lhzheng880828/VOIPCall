package org.jitsi.impl.neomedia.control;

import javax.media.control.FrameProcessingControl;
import org.jitsi.android.util.java.awt.Component;

public class FrameProcessingControlImpl implements FrameProcessingControl {
    private boolean minimalProcessing = false;

    public Component getControlComponent() {
        return null;
    }

    public int getFramesDropped() {
        return 0;
    }

    public boolean isMinimalProcessing() {
        return this.minimalProcessing;
    }

    public void setFramesBehind(float framesBehind) {
        setMinimalProcessing(framesBehind > 0.0f);
    }

    public boolean setMinimalProcessing(boolean minimalProcessing) {
        this.minimalProcessing = minimalProcessing;
        return this.minimalProcessing;
    }
}
