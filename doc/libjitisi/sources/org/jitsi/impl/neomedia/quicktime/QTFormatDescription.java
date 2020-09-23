package org.jitsi.impl.neomedia.quicktime;

import org.jitsi.android.util.java.awt.Dimension;

public class QTFormatDescription extends NSObject {
    public static final String VideoEncodedPixelsSizeAttribute = VideoEncodedPixelsSizeAttribute();

    private static native String VideoEncodedPixelsSizeAttribute();

    private static native Dimension sizeForKey(long j, String str);

    public QTFormatDescription(long ptr) {
        super(ptr);
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        release();
    }

    public Dimension sizeForKey(String key) {
        return sizeForKey(getPtr(), key);
    }
}
