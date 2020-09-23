package org.jitsi.impl.neomedia.quicktime;

public class CVPixelBuffer extends CVImageBuffer {
    private static native int getByteCount(long j);

    private static native int getBytes(long j, long j2, int i);

    private static native byte[] getBytes(long j);

    private static native int getHeight(long j);

    private static native int getWidth(long j);

    public static native void memcpy(byte[] bArr, int i, int i2, long j);

    public CVPixelBuffer(long ptr) {
        super(ptr);
    }

    public int getByteCount() {
        return getByteCount(getPtr());
    }

    public byte[] getBytes() {
        return getBytes(getPtr());
    }

    public int getBytes(long buf, int bufLength) {
        return getBytes(getPtr(), buf, bufLength);
    }

    public int getHeight() {
        return getHeight(getPtr());
    }

    public int getWidth() {
        return getWidth(getPtr());
    }
}
