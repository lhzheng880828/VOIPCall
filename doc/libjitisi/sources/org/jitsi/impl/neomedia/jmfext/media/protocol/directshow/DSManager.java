package org.jitsi.impl.neomedia.jmfext.media.protocol.directshow;

public class DSManager {
    private static DSCaptureDevice[] EMPTY_DEVICES = new DSCaptureDevice[0];
    private DSCaptureDevice[] captureDevices;
    private final long ptr = init();

    private static native void destroy(long j);

    private native long[] getCaptureDevices(long j);

    private static native long init();

    static {
        System.loadLibrary("jndirectshow");
    }

    public DSManager() {
        if (this.ptr == 0) {
            throw new IllegalStateException("ptr");
        }
    }

    public void dispose() {
        destroy(this.ptr);
    }

    public DSCaptureDevice[] getCaptureDevices() {
        if (this.captureDevices == null) {
            long[] ptrs = getCaptureDevices(this.ptr);
            if (ptrs == null || ptrs.length == 0) {
                this.captureDevices = EMPTY_DEVICES;
            } else {
                this.captureDevices = new DSCaptureDevice[ptrs.length];
                for (int i = 0; i < ptrs.length; i++) {
                    this.captureDevices[i] = new DSCaptureDevice(ptrs[i]);
                }
            }
        }
        return this.captureDevices;
    }
}
