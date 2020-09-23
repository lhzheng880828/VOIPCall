package org.jitsi.impl.neomedia.jmfext.media.protocol;

import javax.media.MediaLocator;
import javax.media.control.FrameRateControl;

public abstract class AbstractVideoPushBufferCaptureDevice extends AbstractPushBufferCaptureDevice {
    protected AbstractVideoPushBufferCaptureDevice() {
        this(null);
    }

    protected AbstractVideoPushBufferCaptureDevice(MediaLocator locator) {
        super(locator);
    }

    /* access modifiers changed from: protected */
    public FrameRateControl createFrameRateControl() {
        return null;
    }
}
