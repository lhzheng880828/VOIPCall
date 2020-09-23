package org.jitsi.service.neomedia.device;

import org.jitsi.android.util.java.awt.Dimension;
import org.jitsi.android.util.java.awt.Point;

public interface ScreenDevice {
    boolean containsPoint(Point point);

    int getIndex();

    Dimension getSize();
}
