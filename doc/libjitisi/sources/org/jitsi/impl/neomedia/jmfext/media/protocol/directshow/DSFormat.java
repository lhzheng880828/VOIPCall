package org.jitsi.impl.neomedia.jmfext.media.protocol.directshow;

public class DSFormat {
    public static final int ARGB32 = ARGB32();
    public static final int I420 = I420();
    public static final int MJPG = MJPG();
    public static final int NV12 = NV12();
    public static final int RGB24 = RGB24();
    public static final int RGB32 = RGB32();
    public static final int UYVY = UYVY();
    public static final int Y411 = Y411();
    public static final int Y41P = Y41P();
    public static final int YUY2 = YUY2();
    private final int height;
    private final int pixelFormat;
    private final int width;

    private static native int ARGB32();

    public static native int AYUV();

    private static native int I420();

    public static native int IF09();

    public static native int IMC1();

    public static native int IMC2();

    public static native int IMC3();

    public static native int IMC4();

    public static native int IYUV();

    private static native int MJPG();

    private static native int NV12();

    private static native int RGB24();

    private static native int RGB32();

    private static native int UYVY();

    public static native int Y211();

    private static native int Y411();

    private static native int Y41P();

    private static native int YUY2();

    public static native int YV12();

    public static native int YVU9();

    public static native int YVYU();

    static {
        System.loadLibrary("jndirectshow");
    }

    public DSFormat(int width, int height, int pixelFormat) {
        this.width = width;
        this.height = height;
        this.pixelFormat = pixelFormat;
    }

    public int getHeight() {
        return this.height;
    }

    public int getPixelFormat() {
        return this.pixelFormat;
    }

    public int getWidth() {
        return this.width;
    }

    public String toString() {
        StringBuilder s = new StringBuilder(getClass().getName());
        if (this.pixelFormat != -1) {
            s.append(", pixelFormat 0x");
            s.append(Long.toHexString(((long) this.pixelFormat) & 4294967295L));
        }
        if (this.width != -1) {
            s.append(", width ").append(this.width);
        }
        if (this.height != -1) {
            s.append(", height ").append(this.height);
        }
        return s.toString();
    }
}
