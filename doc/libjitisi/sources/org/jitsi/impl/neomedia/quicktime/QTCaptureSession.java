package org.jitsi.impl.neomedia.quicktime;

public class QTCaptureSession extends NSObject {
    private boolean closed;

    private static native boolean addInput(long j, long j2) throws NSErrorException;

    private static native boolean addOutput(long j, long j2) throws NSErrorException;

    private static native long allocAndInit();

    private static native void startRunning(long j);

    private static native void stopRunning(long j);

    public QTCaptureSession() {
        this(allocAndInit());
    }

    public QTCaptureSession(long ptr) {
        super(ptr);
        this.closed = false;
    }

    public boolean addInput(QTCaptureInput input) throws NSErrorException {
        return addInput(getPtr(), input.getPtr());
    }

    public boolean addOutput(QTCaptureOutput output) throws NSErrorException {
        return addOutput(getPtr(), output.getPtr());
    }

    public synchronized void close() {
        if (!this.closed) {
            stopRunning();
            release();
            this.closed = true;
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        close();
    }

    public void startRunning() {
        startRunning(getPtr());
    }

    public void stopRunning() {
        stopRunning(getPtr());
    }
}
