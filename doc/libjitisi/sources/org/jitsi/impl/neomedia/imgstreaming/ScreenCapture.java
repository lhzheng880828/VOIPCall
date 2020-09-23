package org.jitsi.impl.neomedia.imgstreaming;

import org.jitsi.util.Logger;

public class ScreenCapture {
    private static final Logger logger = Logger.getLogger(ScreenCapture.class);

    public static native boolean grabScreen(int i, int i2, int i3, int i4, int i5, long j, int i6);

    public static native boolean grabScreen(int i, int i2, int i3, int i4, int i5, byte[] bArr);

    static {
        String lib = "jnscreencapture";
        try {
            System.loadLibrary(lib);
        } catch (Throwable t) {
            logger.error("Failed to load native library " + lib + ": " + t.getMessage());
            RuntimeException runtimeException = new RuntimeException(t);
        }
    }
}
