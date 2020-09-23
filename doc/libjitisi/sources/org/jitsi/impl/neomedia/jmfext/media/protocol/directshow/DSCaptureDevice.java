package org.jitsi.impl.neomedia.jmfext.media.protocol.directshow;

public class DSCaptureDevice {
    private static final DSFormat[] EMPTY_FORMATS = new DSFormat[0];
    public static final int S_FALSE = 1;
    public static final int S_OK = 0;
    private final long ptr;

    public interface ISampleGrabberCB {
        void SampleCB(long j, long j2, int i);
    }

    private native void connect(long j);

    private native void disconnect(long j);

    private native DSFormat getFormat(long j);

    private native String getName(long j);

    private native DSFormat[] getSupportedFormats(long j);

    static native int samplecopy(long j, long j2, long j3, int i);

    private native void setDelegate(long j, ISampleGrabberCB iSampleGrabberCB);

    private native int setFormat(long j, DSFormat dSFormat);

    private native int start(long j);

    private native int stop(long j);

    public DSCaptureDevice(long ptr) {
        if (ptr == 0) {
            throw new IllegalArgumentException("ptr");
        }
        this.ptr = ptr;
    }

    public void connect() {
        connect(this.ptr);
    }

    public void disconnect() {
        disconnect(this.ptr);
    }

    public DSFormat getFormat() {
        return getFormat(this.ptr);
    }

    public String getName() {
        return getName(this.ptr).trim();
    }

    public DSFormat[] getSupportedFormats() {
        DSFormat[] formats = getSupportedFormats(this.ptr);
        return formats == null ? EMPTY_FORMATS : formats;
    }

    public void setDelegate(ISampleGrabberCB delegate) {
        setDelegate(this.ptr, delegate);
    }

    public int setFormat(DSFormat format) {
        return setFormat(this.ptr, format);
    }

    public int start() {
        return start(this.ptr);
    }

    public int stop() {
        return stop(this.ptr);
    }
}
