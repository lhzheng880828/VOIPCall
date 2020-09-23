package org.jitsi.impl.neomedia.jmfext.media.protocol;

import javax.media.MediaLocator;
import javax.media.control.FrameRateControl;
import org.jitsi.impl.neomedia.control.FrameRateControlAdapter;

public abstract class AbstractVideoPullBufferCaptureDevice extends AbstractPullBufferCaptureDevice {
    protected AbstractVideoPullBufferCaptureDevice() {
    }

    protected AbstractVideoPullBufferCaptureDevice(MediaLocator locator) {
        super(locator);
    }

    /* access modifiers changed from: protected */
    public FrameRateControl createFrameRateControl() {
        return new FrameRateControlAdapter() {
            private float frameRate = -1.0f;

            public float getFrameRate() {
                return this.frameRate;
            }

            public float setFrameRate(float frameRate) {
                this.frameRate = frameRate;
                return this.frameRate;
            }
        };
    }
}
