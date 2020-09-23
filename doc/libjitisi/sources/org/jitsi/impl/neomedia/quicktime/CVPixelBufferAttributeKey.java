package org.jitsi.impl.neomedia.quicktime;

public final class CVPixelBufferAttributeKey {
    public static final long kCVPixelBufferHeightKey = kCVPixelBufferHeightKey();
    public static final long kCVPixelBufferPixelFormatTypeKey = kCVPixelBufferPixelFormatTypeKey();
    public static final long kCVPixelBufferWidthKey = kCVPixelBufferWidthKey();

    private static native long kCVPixelBufferHeightKey();

    private static native long kCVPixelBufferPixelFormatTypeKey();

    private static native long kCVPixelBufferWidthKey();

    static {
        System.loadLibrary("jnquicktime");
    }

    private CVPixelBufferAttributeKey() {
    }
}
