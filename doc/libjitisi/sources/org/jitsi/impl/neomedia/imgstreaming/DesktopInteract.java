package org.jitsi.impl.neomedia.imgstreaming;

import org.jitsi.android.util.java.awt.image.BufferedImage;

public interface DesktopInteract {
    BufferedImage captureScreen();

    BufferedImage captureScreen(int i, int i2, int i3, int i4);

    boolean captureScreen(int i, int i2, int i3, int i4, int i5, long j, int i6);

    boolean captureScreen(int i, int i2, int i3, int i4, int i5, byte[] bArr);

    boolean captureScreen(int i, long j, int i2);

    boolean captureScreen(int i, byte[] bArr);
}
