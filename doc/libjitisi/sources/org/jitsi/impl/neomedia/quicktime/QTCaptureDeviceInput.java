package org.jitsi.impl.neomedia.quicktime;

public class QTCaptureDeviceInput extends QTCaptureInput {
    private static native long deviceInputWithDevice(long j) throws IllegalArgumentException;

    public QTCaptureDeviceInput(long ptr) {
        super(ptr);
    }

    public static QTCaptureDeviceInput deviceInputWithDevice(QTCaptureDevice device) throws IllegalArgumentException {
        return new QTCaptureDeviceInput(deviceInputWithDevice(device.getPtr()));
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        release();
    }
}
