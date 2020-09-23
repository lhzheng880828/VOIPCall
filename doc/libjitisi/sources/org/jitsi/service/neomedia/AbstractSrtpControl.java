package org.jitsi.service.neomedia;

import org.jitsi.service.neomedia.SrtpControl.TransformEngine;
import org.jitsi.service.neomedia.event.SrtpListener;

public abstract class AbstractSrtpControl<T extends TransformEngine> implements SrtpControl {
    private final SrtpControlType srtpControlType;
    private SrtpListener srtpListener;
    protected T transformEngine;

    public abstract T createTransformEngine();

    protected AbstractSrtpControl(SrtpControlType srtpControlType) {
        if (srtpControlType == null) {
            throw new NullPointerException("srtpControlType");
        }
        this.srtpControlType = srtpControlType;
    }

    public void cleanup() {
        if (this.transformEngine != null) {
            this.transformEngine.cleanup();
            this.transformEngine = null;
        }
    }

    public SrtpControlType getSrtpControlType() {
        return this.srtpControlType;
    }

    public SrtpListener getSrtpListener() {
        return this.srtpListener;
    }

    public T getTransformEngine() {
        if (this.transformEngine == null) {
            this.transformEngine = createTransformEngine();
        }
        return this.transformEngine;
    }

    public void setMasterSession(boolean masterSession) {
    }

    public void setMultistream(SrtpControl master) {
    }

    public void setSrtpListener(SrtpListener srtpListener) {
        this.srtpListener = srtpListener;
    }
}
